/*
 * Copyright 2026 WuJia(Linxu_Link)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wujia.foundation.player

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import timber.log.Timber

/**
 * 统一的播放器控制器，协调视频播放器与多音轨音频播放器组。
 *
 * 该类同时管理两个播放通道：
 * - **视频播放器**：使用 ExoPlayer 在主线程中直接播放，支持背景音乐场景下的视频播放
 * - **音频播放器组**：在控制器内维护多个 ExoPlayer 实例，支持同时播放多个音频流
 *
 * 视频和音频的播放相互独立，但共享音频焦点管理——当音频焦点丢失时，视频也会相应处理。
 *
 * @param context Android 上下文
 * @param config 播放器性能配置，默认为 [VelarisPlayerConfig.Balanced]
 */
class VelarisPlayerController internal constructor(
    context: Context,
    private val config: VelarisPlayerConfig = VelarisPlayerConfig.Balanced,
) {

    private val appContext = context.applicationContext

    /** 当前是否有需要音频焦点的场景音频项 */
    private var hasFocusAudioItems = false

    /** 用户请求的视频音量（非 duck 状态下的音量） */
    private var requestedVideoVolume = DEFAULT_VOLUME

    /** 临时失去焦点时是否需要恢复播放 */
    private var resumeVideoOnFocusGain = false

    private var currentVideoUri: String? = null
    private var currentAudioItems: List<AudioMediaItem> = emptyList()
    private val currentAudioVolumes = mutableMapOf<String, Float>()
    private var currentAudioMasterVolume = DEFAULT_VOLUME

    private var clockAudioItem: AudioMediaItem? = null
    private var clockAudioActive = false
    private var currentPlaybackAudioItems: List<AudioMediaItem> = emptyList()
    private val audioPlayers = linkedMapOf<String, ExoPlayer>()
    private val audioItems = linkedMapOf<String, AudioMediaItem>()
    private val endedAudioIds = mutableSetOf<String>()
    private val audioItemListeners = mutableMapOf<String, Player.Listener>()

    var isVideoFirstFrameRendered by mutableStateOf(false)
        private set

    /** 当前视频是否正在播放，Compose 可观察状态 */
    var isVideoPlaying by mutableStateOf(false)
        private set

    /**
     * 是否至少已成功渲染过视频首帧。
     * 用于区分三种状态（解决暂停时闪回首帧的问题）：
     * - false + !isScenePlaying：从未播放过 → 显示静态首帧/背景（避免过早 attach PlayerView 产生音频日志）
     * - true + !isScenePlaying：播放过且当前暂停 → 保留 VelarisVideoPlayer，显示 ExoPlayer 当前暂停帧
     * - true + 播放中：正常实时视频
     *
     * 该标志在 setVideoUri 或 release 时会被重置。只有真正 play 并渲染后才置 true。
     */
    val hasVideoEverRenderedFirstFrame: Boolean
        get() = isVideoFirstFrameRendered

    /** 当前视频播放器是否正在播放（内部可观察状态）。 */
    val videoIsPlaying: Boolean
        get() = isVideoPlaying

    private var _audioSessionId by mutableIntStateOf(C.INDEX_UNSET)

    // ============================================================
    // 视频播放器
    // ============================================================

    /**
     * 视频播放器实例，用于播放视频内容。
     * 配置了音频属性（电影类型）、处理音频线缆拔除事件、循环播放。
     */
    private val videoPlayerListener =
        object : Player.Listener {
            override fun onRenderedFirstFrame() {
                isVideoFirstFrameRendered = true
            }
        }

    internal val videoPlayer: ExoPlayer =
        ExoPlayer.Builder(
            appContext,
            DefaultRenderersFactory(appContext)
                .setEnableDecoderFallback(config.enableDecoderFallback),
        )
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                false,
            )
            .setHandleAudioBecomingNoisy(true)
            .build()
            .apply {
                repeatMode = Player.REPEAT_MODE_ONE
                addListener(videoPlayerListener)
            }

    // ============================================================
    // 音频焦点管理
    // ============================================================

    /**
     * 音频焦点控制器，负责处理与其他音频应用的竞争。
     *
     * 焦点策略：
     * - 获取焦点时：恢复视频播放音量，若之前因临时失去焦点而暂停则恢复播放
     * - 永久失去焦点时：暂停视频播放
     * - 临时失去焦点时：暂停视频，记录是否需要恢复
     * - Duck 状态时：降低视频音量至 20%，使其他音频可以覆盖
     */
    private val audioFocusController =
        AudioFocusController(
            context = appContext,
            onFocusGained = {
                videoPlayer.volume = requestedVideoVolume
                if (resumeVideoOnFocusGain) {
                    resumeVideoOnFocusGain = false
                    videoPlayer.play()
                    isVideoPlaying = true
                }
            },
            onFocusLost = {
                resumeVideoOnFocusGain = false
                videoPlayer.pause()
                isVideoPlaying = false
            },
            onFocusLostTransient = {
                resumeVideoOnFocusGain = videoPlayer.isPlaying
                videoPlayer.pause()
                isVideoPlaying = false
            },
            onDuck = {
                videoPlayer.volume = requestedVideoVolume * DUCK_VOLUME_MULTIPLIER
            },
        )

    // ============================================================
    // 公共 API：音频会话
    // ============================================================

    /**
     * 当前主音频播放器的 audioSessionId，可用于需要绑定播放会话的音频效果。
     * 在音频播放器准备并开始输出后才有有效值，否则返回 [C.INDEX_UNSET]。
     */
    val audioSessionId: Int
        get() = _audioSessionId

    // ============================================================
    // 公共 API：视频控制
    // ============================================================

    /**
     * 设置视频 URI。如果传入空或空白字符串，则清除当前视频。
     * @param uri 视频资源的 URI，支持网络 URL 或本地路径
     */
    fun setVideoUri(uri: String?) {
        val normalizedUri = uri?.takeIf { it.isNotBlank() }
        if (currentVideoUri == normalizedUri) return

        currentVideoUri = normalizedUri
        isVideoFirstFrameRendered = false
        if (normalizedUri == null) {
            videoPlayer.clearMediaItems()
            return
        }

        videoPlayer.setMediaItem(MediaItem.fromUri(normalizedUri.toUri()))
        // 延迟 prepare：仅在 play() 时才 prepare，避免应用启动/闲置时就为视频创建 AudioTrack，
        // 导致系统 AudioTrackImpl 持续打印 [audioTrackData] 日志。即使不播放任何场景也会 spam。
    }

    private fun ensureVideoPrepared() {
        if (currentVideoUri != null && videoPlayer.playbackState == Player.STATE_IDLE) {
            videoPlayer.prepare()
        }
    }

    // ============================================================
    // 公共 API：音频控制
    // ============================================================

    /**
     * 设置待播放的音频列表（场景主音频）。
     *
     * @param items 音频媒体项列表，id 必须唯一
     */
    fun setAudioItems(items: List<AudioMediaItem>) {
        val cappedItems = if (config.maxSimultaneousAudioTracks < items.size) {
            items.take(config.maxSimultaneousAudioTracks)
        } else {
            items
        }
        val normalizedItems = cappedItems.map { it.copy(volume = it.volume.coercePlayerVolume()) }
        normalizedItems.requireUniqueAudioIds()
        if (currentAudioItems == normalizedItems) return

        Timber.tag(TAG).d(
            "setAudioItems count=%d ids=%s volumes=%s",
            normalizedItems.size,
            normalizedItems.map { it.id },
            normalizedItems.map { it.volume },
        )

        currentAudioItems = normalizedItems
        currentAudioVolumes.clear()
        currentAudioVolumes.putAll(normalizedItems.associate { it.id to it.volume })
        hasFocusAudioItems = normalizedItems.any { !it.noFocus }
        setPlaybackAudioItems(currentPlaybackItems(), playWhenReady = isVideoPlaying)
        if (clockAudioActive) {
            upsertClockAudioItem(playWhenReady = isVideoPlaying)
        }
    }

    /**
     * 开始播放。
     * - 请求音频焦点，失败时直接返回不播放
     * - 播放视频（如果已设置）
     * - 播放所有当前音频
     */
    fun play() {
        val playbackItems = currentPlaybackItems()
        Timber.tag(TAG).d(
            "play request hasFocusAudioItems=%s currentAudioIds=%s videoUri=%s isVideoPlaying=%s",
            hasFocusAudioItems,
            playbackItems.map { it.id },
            currentVideoUri,
            isVideoPlaying,
        )
        if (hasFocusAudioItems && !audioFocusController.request()) return
        ensureVideoPrepared()
        videoPlayer.play()
        isVideoPlaying = true
        setPlaybackAudioItems(playbackItems, playWhenReady = true)
        playAllAudio()
    }

    /**
     * 暂停播放。
     * - 暂停视频
     * - 暂停音频播放器组
     * - 释放音频焦点
     */
    fun pause() {
        Timber.tag(TAG).d(
            "pause request currentAudioIds=%s videoUri=%s isVideoPlaying=%s",
            currentAudioItems.map { it.id },
            currentVideoUri,
            isVideoPlaying,
        )
        videoPlayer.pause()
        isVideoPlaying = false
        pauseAllAudio()
        audioFocusController.abandon()
    }

    /**
     * 仅暂停视频播放，不影响音频播放器组和音频焦点。
     * 供 VelarisPlayerPool.pauseAllVideos() / pauseVideosExcept() 在滑动手势中临时冻结视频画面使用
     * （此时通常仍希望“当前”页的音频继续，直到页面真正提交切换）。
     * 实际跨页导航时，PlayerMediaCoordinator 会调用 pauseExcept() 对旧页执行完整 pause()（含音频停止）。
     */
    fun pauseVideo() {
        videoPlayer.pause()
    }

    /**
     * 停止所有音频播放。
     */
    fun stopAudio() {
        stopAllAudio()
    }

    // ============================================================
    // 公共 API：时钟音频控制（不请求焦点）
    // ============================================================

    /**
     * 设置时钟音频（翻页时钟音效）。
     * 该音频作为普通 AudioMediaItem 交给控制器管理，但标记为 noFocus，不参与音频焦点申请。
     * @param uri 音频资源 URI，null 时停止并释放时钟播放器
     * @param volume 音量值 0f ~ 1f
     */
    fun setClockAudio(uri: String?, volume: Float = 0.5f) {
        val normalizedUri = uri?.takeIf { it.isNotBlank() }
        if (normalizedUri == null) {
            stopClockAudio()
            clockAudioItem = null
            return
        }

        val normalizedVolume = volume.coercePlayerVolume()
        val newItem = AudioMediaItem(
            id = CLOCK_AUDIO_ID,
            uri = normalizedUri,
            volume = normalizedVolume,
            title = CLOCK_AUDIO_ID,
            loop = true,
            noFocus = true,
        )
        if (clockAudioItem == newItem) return

        clockAudioItem = newItem
        if (clockAudioActive) {
            upsertClockAudioItem(playWhenReady = true)
        }
    }

    /**
     * 播放时钟音频。如果播放器已准备好，直接开始播放。
     * 不请求音频焦点，不影响场景主音频。
     */
    fun playClockAudio() {
        clockAudioActive = true
        upsertClockAudioItem(playWhenReady = true)
        Timber.tag(TAG).d(
            "playClockAudio started item=%s sceneAudioIds=%s",
            clockAudioItem?.id,
            currentAudioItems.map { it.id },
        )
    }

    /**
     * 设置时钟音频音量。
     * @param volume 音量值 0f ~ 1f
     */
    fun setClockAudioVolume(volume: Float) {
        val item = clockAudioItem ?: return
        val normalizedVolume = volume.coercePlayerVolume()
        if (item.volume == normalizedVolume) return

        clockAudioItem = item.copy(volume = normalizedVolume)
        if (clockAudioActive) {
            setAudioVolume(CLOCK_AUDIO_ID, normalizedVolume)
        }
    }

    /**
     * 停止时钟音频播放并释放播放器。
     */
    fun stopClockAudio() {
        if (!clockAudioActive) return
        clockAudioActive = false
        removeAudioItem(CLOCK_AUDIO_ID)
        Timber.tag(TAG).d("stopClockAudio stopped")
    }

    // ============================================================
    // 公共 API：音量控制
    // ============================================================

    /**
     * 设置视频播放音量。
     * @param volume 音量值，范围 0f ~ 1f，会被限制在有效范围内
     */
    fun setVideoVolume(volume: Float) {
        val normalizedVolume = volume.coercePlayerVolume()
        if (requestedVideoVolume == normalizedVolume) return

        requestedVideoVolume = normalizedVolume
        videoPlayer.volume = normalizedVolume
    }

    /**
     * 设置指定音频的音量。
     * @param audioId 目标音频的唯一标识符
     * @param volume 音量值，范围 0f ~ 1f
     */
    fun setAudioVolume(audioId: String, volume: Float) {
        val normalizedVolume = volume.coercePlayerVolume()
        if (currentAudioVolumes[audioId] == normalizedVolume) return

        currentAudioVolumes[audioId] = normalizedVolume
        currentAudioItems = currentAudioItems.map { item ->
            if (item.id == audioId) item.copy(volume = normalizedVolume) else item
        }
        audioItems[audioId]?.let { item ->
            audioItems[audioId] = item.copy(volume = normalizedVolume)
            currentPlaybackAudioItems = audioItems.values.toList()
        }
        setPlayerVolume(audioId, normalizedVolume)
    }

    /**
     * 设置所有音频的主音量。
     * @param volume 音量值，范围 0f ~ 1f
     */
    fun setAudioMasterVolume(volume: Float) {
        val normalizedVolume = volume.coercePlayerVolume()
        if (currentAudioMasterVolume == normalizedVolume) return

        currentAudioMasterVolume = normalizedVolume
        refreshAllPlayerVolumes()
    }

    // ============================================================
    // 生命周期
    // ============================================================

    /**
     * 释放控制器拥有的全部资源（音频播放器 + 视频播放器 + 音频焦点）。
     *
     * 历史上存在 `releaseVideoOnly()` + `releaseAudio = false` 分支（用于“保留后台 Service”），
     * 但 Service 已移除，该死分支已被删除。所有调用方都应走完整 release，避免资源泄漏。
     */
    fun release() {
        videoPlayer.pause()
        audioFocusController.abandon()

        releaseAudioPlayers()
        currentAudioItems = emptyList()
        currentPlaybackAudioItems = emptyList()
        currentAudioVolumes.clear()
        currentAudioMasterVolume = DEFAULT_VOLUME
        clockAudioActive = false
        clockAudioItem = null

        currentVideoUri = null
        isVideoFirstFrameRendered = false
        isVideoPlaying = false
        videoPlayer.removeListener(videoPlayerListener)
        videoPlayer.release()
    }

    // ============================================================
    // 内部工具方法
    // ============================================================

    private fun currentPlaybackItems(): List<AudioMediaItem> = if (clockAudioActive) {
        clockAudioItem?.let { currentAudioItems + it } ?: currentAudioItems
    } else {
        currentAudioItems
    }

    private fun upsertClockAudioItem(playWhenReady: Boolean) {
        val item = clockAudioItem ?: return
        upsertAudioItem(item, playWhenReady)
    }

    private fun setPlaybackAudioItems(items: List<AudioMediaItem>, playWhenReady: Boolean) {
        items.requireUniqueAudioIds()
        if (currentPlaybackAudioItems == items) return

        removeAudioItemListeners()
        releaseAudioPlayers()
        currentPlaybackAudioItems = items

        if (items.isEmpty()) {
            refreshAudioSessionId()
            return
        }

        items.forEach { item ->
            val player = buildAudioPlayer()
            player.setMediaItem(item.toMediaItem())
            player.repeatMode = if (item.loop) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
            player.volume = item.effectiveVolume()
            val listener = item.toPlayerListener()
            player.addListener(listener)
            // 仅在 playWhenReady 时立即 prepare；bind 阶段（!playWhenReady）只 setMediaItem 不 prepare，
            // 避免刚打开应用（场景自动 bind）就为所有环境音创建 ExoPlayer + AudioTrack，产生持续日志 spam。
            if (playWhenReady) {
                player.prepare()
            }

            audioPlayers[item.id] = player
            audioItems[item.id] = item
            audioItemListeners[item.id] = listener
        }
        endedAudioIds.clear()
        if (playWhenReady) {
            playAllAudio()
        }
        refreshAudioSessionId()
    }

    private fun upsertAudioItem(item: AudioMediaItem, playWhenReady: Boolean) {
        val existingPlayer = audioPlayers[item.id]
        val existingItem = audioItems[item.id]
        val player = existingPlayer ?: buildAudioPlayer()

        Timber.tag(TAG).d(
            "upsertAudioItem id=%s hasExisting=%s playWhenReady=%s volume=%s",
            item.id,
            existingPlayer != null,
            playWhenReady,
            item.volume,
        )

        if (existingItem?.uri != item.uri) {
            player.setMediaItem(item.toMediaItem())
            if (playWhenReady) {
                player.prepare()
            }
        }
        player.repeatMode = if (item.loop) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
        player.volume = item.effectiveVolume()
        if (existingPlayer == null) {
            val listener = item.toPlayerListener()
            player.addListener(listener)
            audioItemListeners[item.id] = listener
        }
        audioPlayers[item.id] = player
        audioItems[item.id] = item
        currentPlaybackAudioItems = audioItems.values.toList()
        endedAudioIds -= item.id

        if (playWhenReady) {
            if (player.playbackState == Player.STATE_IDLE) {
                player.prepare()
            }
            player.playWhenReady = true
            player.play()
        }
        refreshAudioSessionId()
    }

    private fun removeAudioItem(id: String) {
        val player = audioPlayers.remove(id) ?: return
        audioItemListeners.remove(id)?.let(player::removeListener)
        audioItems.remove(id)
        currentPlaybackAudioItems = audioItems.values.toList()
        endedAudioIds -= id
        player.pause()
        player.clearMediaItems()
        player.release()
        refreshAudioSessionId()
    }

    private fun playAllAudio() {
        Timber.tag(TAG).d(
            "playAllAudio start playerCount=%d audioIds=%s endedIds=%s",
            audioPlayers.size,
            audioPlayers.keys.toList(),
            endedAudioIds.toList(),
        )
        audioPlayers.values.forEach { player ->
            if (player.playbackState == Player.STATE_IDLE) {
                player.prepare()
            }
            if (player.playbackState == Player.STATE_ENDED) {
                player.seekTo(0)
                player.prepare()
            }
            player.playWhenReady = true
            player.play()
        }
        endedAudioIds.clear()
        refreshAudioSessionId()
    }

    private fun pauseAllAudio() {
        Timber.tag(TAG).d(
            "pauseAllAudio start playerCount=%d audioIds=%s",
            audioPlayers.size,
            audioPlayers.keys.toList(),
        )
        audioPlayers.values.forEach { it.pause() }
        refreshAudioSessionId()
    }

    private fun stopAllAudio() {
        Timber.tag(TAG).d(
            "stopAllAudio start playerCount=%d audioIds=%s",
            audioPlayers.size,
            audioPlayers.keys.toList(),
        )
        releaseAudioPlayers()
    }

    private fun releaseAudioPlayers() {
        removeAudioItemListeners()
        audioPlayers.values.forEach { player ->
            player.pause()
            player.clearMediaItems()
            player.release()
        }
        audioPlayers.clear()
        audioItems.clear()
        currentPlaybackAudioItems = emptyList()
        endedAudioIds.clear()
        refreshAudioSessionId()
    }

    private fun removeAudioItemListeners() {
        audioPlayers.forEach { (id, player) ->
            audioItemListeners[id]?.let(player::removeListener)
        }
        audioItemListeners.clear()
    }

    private fun setPlayerVolume(audioId: String, volume: Float) {
        audioPlayers[audioId]?.volume = volume * currentAudioMasterVolume
    }

    private fun refreshAllPlayerVolumes() {
        audioPlayers.forEach { (id, player) ->
            val item = audioItems[id] ?: return@forEach
            player.volume = item.effectiveVolume()
        }
    }

    private fun buildAudioPlayer(): ExoPlayer = ExoPlayer.Builder(appContext)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build(),
            false,
        )
        .setHandleAudioBecomingNoisy(true)
        .build()

    private fun refreshAudioSessionId() {
        // 优先选择非 clock-audio 的主场景音频的 sessionId。
        // 只有当当前只剩时钟音频时，才回退到它的 session。
        // 避免时钟音频（noFocus）干扰主场景的 audioSessionId（可能被 visualizer 等使用）。
        val primaryEntry = audioPlayers.entries.firstOrNull { it.key != CLOCK_AUDIO_ID }
        val primaryPlayer = primaryEntry?.value ?: audioPlayers.values.firstOrNull()
        val sessionId = primaryPlayer?.audioSessionId ?: C.INDEX_UNSET
        _audioSessionId = if (sessionId > 0) sessionId else C.INDEX_UNSET
        Timber.tag(TAG).d(
            "refreshAudioSessionId sessionId=%d (primary=%s)",
            _audioSessionId,
            primaryEntry?.key ?: "clock-only",
        )
    }

    private fun AudioMediaItem.effectiveVolume(): Float = volume.coercePlayerVolume() * currentAudioMasterVolume

    private fun AudioMediaItem.toPlayerListener(): Player.Listener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState != Player.STATE_ENDED) return

            endedAudioIds += id
            if (stopAllOnEnd) {
                stopAllAudio()
                return
            }

            val nonLoopingIds = audioItems.values
                .filterNot { it.loop }
                .mapTo(mutableSetOf()) { it.id }

            // 只有当“当前列表中不存在任何循环音频”时，才在所有非循环音频结束后自动停止全部。
            // 这是对旧 VelarisAudioPlaybackService 行为的恢复（旧逻辑：nonLoopingIds.size == audioItems.size）。
            // 避免“背景循环音 + 有限音效”场景下，音效播完就把循环也一起停掉的回归。
            val hasAnyLooping = audioItems.values.any { it.loop }
            if (!hasAnyLooping && nonLoopingIds.isNotEmpty() && endedAudioIds.containsAll(nonLoopingIds)) {
                stopAllAudio()
            }
        }
    }

    private fun AudioMediaItem.toMediaItem(): MediaItem = MediaItem.Builder()
        .setMediaId(id)
        .setUri(uri.toUri())
        .build()

    companion object {
        private const val TAG = "ScenePlayerCtrl"

        /** Duck 状态下音量降低至原音量的 20% */
        private const val DUCK_VOLUME_MULTIPLIER = 0.2f
        private const val CLOCK_AUDIO_ID = "clock-audio"
    }
}
