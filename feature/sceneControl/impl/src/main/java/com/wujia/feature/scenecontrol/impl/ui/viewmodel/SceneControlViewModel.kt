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
package com.wujia.feature.scenecontrol.impl.ui.viewmodel

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wujia.foundation.domain.scene.UpdateSceneControlSettingsUseCase
import com.wujia.foundation.model.scene.NO_PARTICLE
import com.wujia.foundation.model.scene.SceneControlDefaults
import com.wujia.foundation.model.scene.SceneControlSettings
import com.wujia.foundation.model.scene.SceneCountdownClockPosition
import com.wujia.foundation.model.scene.SceneParticleEffect
import com.wujia.foundation.model.scene.SceneParticleQuality
import com.wujia.foundation.model.scene.SceneParticleSettings
import com.wujia.foundation.model.scene.SceneTimerMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ControlEvent {
    data object TimerExpired : ControlEvent
}

/** 定时时长选项（毫秒），与 SceneTimePanel 的选项索引对应 */
private val TIMER_DURATIONS = longArrayOf(
    15 * 60 * 1000L, // 0: 15分钟
    25 * 60 * 1000L, // 1: 25分钟
    45 * 60 * 1000L, // 2: 45分钟
    0L, // 3: 自定义
)

private const val DEFAULT_TIMER_OPTION = 2
private val DEFAULT_TIMER_DURATION_MILLIS = TIMER_DURATIONS[DEFAULT_TIMER_OPTION]

@Stable
/**
 * 场景控制面板的 UI 状态。
 *
 * 设计要点：
 * - 使用绝对时间戳（timerEndTimestampMillis）实现后台计时不漂移。
 * - timerTick 用于驱动 UI 强制刷新（因为剩余时间是计算属性）。
 * - timerRemainingMillis / timerProgress 是计算属性，供 UI 实时展示。
 * - 支持倒计时（Countdown）和时钟（Clock）两种模式。
 */
data class SceneControlUiState(
    val timerMode: SceneTimerMode = SceneTimerMode.Countdown,
    val timerSelectedOption: Int = DEFAULT_TIMER_OPTION,
    val timerDurationMillis: Long = DEFAULT_TIMER_DURATION_MILLIS,
    val pausedRemainingMillis: Long? = null,
    val timerEndTimestampMillis: Long = 0L,
    val currentTimestampMillis: Long = 0L,
    val isTimerRunning: Boolean = false,
    val showCustomTimerDialog: Boolean = false,
    val showCountdownClock: Boolean = true,
    val alarmReminderEnabled: Boolean = false,
    val countdownClockPosition: SceneCountdownClockPosition = SceneCountdownClockPosition.Center,
    val clockAudioVolume: Float = SceneControlDefaults.CLOCK_AUDIO_VOLUME,
    val brightness: Float = SceneControlDefaults.BRIGHTNESS,
    val darkness: Float = SceneControlDefaults.DARKNESS,
    /** 粒子配置（雨/雪/萤火虫等） */
    val particleSettings: SceneParticleSettings = NO_PARTICLE,
    /** 递增计数器，用于驱动 StateFlow 发射以刷新 UI（因为剩余时间是纯计算属性） */
    val timerTick: Long = 0L,
) {
    /** 基于绝对时间戳计算当前剩余毫秒，支持后台计时不漂移 */
    val timerRemainingMillis: Long
        get() = when {
            timerMode != SceneTimerMode.Countdown -> timerDurationMillis
            isTimerRunning && timerEndTimestampMillis > 0 -> {
                (timerEndTimestampMillis - currentTimestampMillis).coerceAtLeast(0)
            }
            pausedRemainingMillis != null -> pausedRemainingMillis
            else -> timerDurationMillis
        }

    /** 倒计时进度，1f = 满，0f = 归零 */
    val timerProgress: Float
        get() = if (timerDurationMillis > 0) {
            (timerRemainingMillis.toFloat() / timerDurationMillis).coerceIn(0f, 1f)
        } else {
            1f
        }
}

/**
 * 可注入的时间源（默认使用 System.currentTimeMillis）。
 * 通过 open + @Inject 设计，便于单元测试时 mock 当前时间。
 */
open class SceneControlClock @Inject constructor() {
    open fun currentTimeMillis(): Long = System.currentTimeMillis()
}

/**
 * 场景控制 ViewModel。
 *
 * 核心职责：
 * - 维护定时器状态（倒计时 / 时钟模式），使用绝对时间戳支持后台运行。
 * - 响应用户操作（切换定时选项、播放暂停、粒子调节等）并立即持久化。
 * - 通过 Channel 向上层发送一次性事件（如 TimerExpired）。
 * - 提供 SceneControlClock 可注入，便于测试（可 mock 时间）。
 *
 * 设计要点：
 * - 使用 timerJob 统一管理 ticker，避免多处重复 cancel/start。
 * - persistControlSettings 集中处理持久化（使用 UseCase）。
 * - onSceneChanged 区分“首次进入”和“同场景更新”，避免不必要的重置。
 */
@HiltViewModel
class SceneControlViewModel @Inject constructor(
    private val updateSceneControlSettings: UpdateSceneControlSettingsUseCase,
    private val sceneControlClock: SceneControlClock,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SceneControlUiState())
    val uiState: StateFlow<SceneControlUiState> = _uiState.asStateFlow()

    private val _events = Channel<ControlEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var activeSceneId: String? = null
    private var guideCompleted: Boolean = false

    /**
     * 场景切换或设置变更时由上层调用。
     * - 不同 sceneId：完全重置状态（包括取消定时器）。
     * - 相同 sceneId：合并持久化的设置（保护正在运行的倒计时不被覆盖）。
     *
     * 负责启动对应模式的 ticker（倒计时或时钟）。
     */
    fun onSceneChanged(
        sceneId: String?,
        brightness: Float,
        darkness: Float,
        timerMode: SceneTimerMode = SceneTimerMode.Countdown,
        timerDurationMillis: Long,
        particleSettings: SceneParticleSettings = NO_PARTICLE,
        guideCompleted: Boolean = false,
        showCountdownClock: Boolean = true,
        alarmReminderEnabled: Boolean = false,
        countdownClockPosition: SceneCountdownClockPosition = SceneCountdownClockPosition.Center,
        clockAudioVolume: Float = SceneControlDefaults.CLOCK_AUDIO_VOLUME,
    ) {
        if (sceneId == null) return
        if (activeSceneId != sceneId) {
            // 不同场景：完整重置（取消旧定时器，恢复默认运行状态）
            activeSceneId = sceneId
            this.guideCompleted = guideCompleted
            cancelTimerJob()
            _uiState.update {
                it.copy(
                    brightness = brightness,
                    darkness = darkness,
                    particleSettings = particleSettings,
                    timerMode = timerMode,
                    timerSelectedOption = timerDurationMillis.toTimerOptionIndex(),
                    timerDurationMillis = timerDurationMillis,
                    pausedRemainingMillis = null,
                    timerEndTimestampMillis = 0L,
                    currentTimestampMillis = sceneControlClock.currentTimeMillis(),
                    isTimerRunning = false,
                    showCountdownClock = showCountdownClock,
                    alarmReminderEnabled = alarmReminderEnabled && timerMode == SceneTimerMode.Countdown,
                    countdownClockPosition = countdownClockPosition,
                    clockAudioVolume = clockAudioVolume,
                    showCustomTimerDialog = false,
                )
            }
            if (timerMode == SceneTimerMode.Clock) {
                startClockTicker()
            }
        } else {
            // 同场景：合并持久化设置，但保护正在运行的倒计时
            this.guideCompleted = guideCompleted
            _uiState.update {
                it.copy(
                    brightness = brightness,
                    darkness = darkness,
                    particleSettings = particleSettings,
                    timerMode = timerMode,
                    showCountdownClock = showCountdownClock,
                    alarmReminderEnabled = alarmReminderEnabled && timerMode == SceneTimerMode.Countdown,
                    countdownClockPosition = countdownClockPosition,
                    clockAudioVolume = clockAudioVolume,
                    // 正在运行的倒计时时，保留本地剩余时间，不被上游覆盖
                    timerSelectedOption = if (it.isTimerRunning) {
                        it.timerSelectedOption
                    } else {
                        timerDurationMillis.toTimerOptionIndex()
                    },
                    timerDurationMillis = if (it.isTimerRunning) {
                        it.timerDurationMillis
                    } else {
                        timerDurationMillis
                    },
                )
            }
            if (timerMode == SceneTimerMode.Clock) {
                startClockTicker()
            } else if (!_uiState.value.isTimerRunning) {
                cancelTimerJob()
            }
        }
    }

    fun onTimerOptionChange(index: Int, resumeIfPlaying: Boolean = false) {
        if (index == 3) {
            _uiState.update { it.copy(showCustomTimerDialog = true) }
            return
        }
        val duration = TIMER_DURATIONS.getOrElse(index) { 0L }
        timerJob?.cancel()
        _uiState.update {
            it.copy(
                timerMode = SceneTimerMode.Countdown,
                timerSelectedOption = index,
                timerDurationMillis = duration,
                pausedRemainingMillis = null,
                timerEndTimestampMillis = 0L,
                currentTimestampMillis = sceneControlClock.currentTimeMillis(),
                isTimerRunning = false,
            )
        }
        persistControlSettings()
        if (resumeIfPlaying) {
            onPlaybackStarted()
        }
    }

    fun onCustomTimerDialogVisibilityChange(visible: Boolean) {
        _uiState.update { it.copy(showCustomTimerDialog = visible) }
    }

    fun onCustomTimerConfirm(hours: Int, minutes: Int, resumeIfPlaying: Boolean = false) {
        val duration = (hours * 60L + minutes) * 60 * 1000L
        if (duration <= 0) return
        timerJob?.cancel()
        _uiState.update {
            it.copy(
                timerMode = SceneTimerMode.Countdown,
                timerSelectedOption = 3,
                timerDurationMillis = duration,
                pausedRemainingMillis = null,
                timerEndTimestampMillis = 0L,
                currentTimestampMillis = sceneControlClock.currentTimeMillis(),
                isTimerRunning = false,
                showCustomTimerDialog = false,
            )
        }
        persistControlSettings()
        if (resumeIfPlaying) {
            onPlaybackStarted()
        }
    }

    fun onClockTimerConfirm() {
        timerJob?.cancel()
        _uiState.update {
            it.copy(
                timerMode = SceneTimerMode.Clock,
                pausedRemainingMillis = null,
                timerEndTimestampMillis = 0L,
                currentTimestampMillis = sceneControlClock.currentTimeMillis(),
                isTimerRunning = false,
                alarmReminderEnabled = false,
                showCustomTimerDialog = false,
            )
        }
        persistControlSettings()
        startClockTicker()
    }

    /**
     * 切换定时器运行/暂停状态。
     * 倒计时模式下会使用绝对结束时间戳，便于后台恢复。
     */
    fun onTimerToggle() {
        val state = _uiState.value
        if (state.isTimerRunning) {
            cancelTimerJob()
            val now = sceneControlClock.currentTimeMillis()
            _uiState.update {
                val remainingMillis = (it.timerEndTimestampMillis - now).coerceAtLeast(0)
                it.copy(
                    isTimerRunning = false,
                    currentTimestampMillis = now,
                    pausedRemainingMillis = remainingMillis,
                    timerEndTimestampMillis = 0L,
                )
            }
        } else if (state.timerMode == SceneTimerMode.Countdown && state.timerRemainingMillis > 0) {
            val now = sceneControlClock.currentTimeMillis()
            val endTime = now + state.timerRemainingMillis
            _uiState.update {
                it.copy(
                    isTimerRunning = true,
                    pausedRemainingMillis = null,
                    timerEndTimestampMillis = endTime,
                    currentTimestampMillis = now,
                )
            }
            startCountdown()
        }
    }

    /**
     * 播放开始时调用（由上层 scene 通知）。
     * - 时钟模式：启动时钟 ticker
     * - 倒计时模式：若有剩余时间则启动倒计时
     */
    fun onPlaybackStarted() {
        val state = _uiState.value
        if (state.timerMode == SceneTimerMode.Clock) {
            startClockTicker()
            return
        }
        if (!state.isTimerRunning && state.timerRemainingMillis > 0) {
            val now = sceneControlClock.currentTimeMillis()
            val endTime = now + state.timerRemainingMillis
            _uiState.update {
                it.copy(
                    isTimerRunning = true,
                    pausedRemainingMillis = null,
                    timerEndTimestampMillis = endTime,
                    currentTimestampMillis = now,
                )
            }
            startCountdown()
        }
    }

    /**
     * 播放暂停时调用（由上层 scene 通知）。
     * 若倒计时正在运行则暂停并记录剩余时间；否则取消时钟 ticker。
     */
    fun onPlaybackPaused() {
        val state = _uiState.value
        if (state.isTimerRunning) {
            cancelTimerJob()
            val now = sceneControlClock.currentTimeMillis()
            _uiState.update {
                val remainingMillis = (it.timerEndTimestampMillis - now).coerceAtLeast(0)
                it.copy(
                    isTimerRunning = false,
                    currentTimestampMillis = now,
                    pausedRemainingMillis = remainingMillis,
                    timerEndTimestampMillis = 0L,
                )
            }
        } else if (state.timerMode != SceneTimerMode.Clock) {
            cancelTimerJob()
        }
    }

    fun onShowCountdownClockChange(enabled: Boolean) {
        _uiState.update { it.copy(showCountdownClock = enabled) }
        persistControlSettings()
    }

    fun onAlarmReminderChange(enabled: Boolean) {
        _uiState.update {
            it.copy(
                alarmReminderEnabled = enabled && it.timerMode == SceneTimerMode.Countdown,
            )
        }
        persistControlSettings()
    }

    fun onCountdownClockPositionChange(position: SceneCountdownClockPosition) {
        _uiState.update { it.copy(countdownClockPosition = position) }
        persistControlSettings()
    }

    fun onClockAudioVolumeChange(volume: Float) {
        _uiState.update { it.copy(clockAudioVolume = volume.coerceIn(0f, 1f)) }
        persistControlSettings()
    }

    fun onVisualControlItemValueChange(index: Int, value: Float) {
        val clamped = value.coerceIn(0f, 1f)
        _uiState.update { state ->
            when (index) {
                0 -> state.copy(brightness = clamped)
                1 -> state.copy(darkness = clamped)
                else -> return@update state
            }
        }
        persistControlSettings()
    }

    fun onParticleEffectChange(effect: SceneParticleEffect) {
        _uiState.update {
            it.copy(
                particleSettings = it.particleSettings.copy(effect = effect),
            )
        }
        persistControlSettings()
    }

    fun onParticleIntensityChange(intensity: Float) {
        _uiState.update {
            it.copy(
                particleSettings = it.particleSettings.copy(intensity = intensity.coerceIn(0f, 1f)),
            )
        }
        persistControlSettings()
    }

    fun onParticleWindChange(wind: Float) {
        _uiState.update {
            it.copy(
                particleSettings = it.particleSettings.copy(wind = wind.coerceIn(0f, 1f)),
            )
        }
        persistControlSettings()
    }

    fun onParticleQualityChange(quality: SceneParticleQuality) {
        _uiState.update {
            it.copy(
                particleSettings = it.particleSettings.copy(quality = quality),
            )
        }
        persistControlSettings()
    }

    fun onParticleForegroundGlassChange(enabled: Boolean) {
        _uiState.update {
            it.copy(
                particleSettings = it.particleSettings.copy(foregroundGlassEnabled = enabled),
            )
        }
        persistControlSettings()
    }

    /**
     * 集中持久化当前控制设置。
     * 所有用户操作（亮度、定时器、粒子、闹钟等）变更后都应调用此方法。
     * 使用 runCatching 静默失败（生产环境可考虑上报）。
     */
    private fun persistControlSettings() {
        val sceneId = activeSceneId ?: return
        val state = _uiState.value
        viewModelScope.launch {
            runCatching {
                updateSceneControlSettings(
                    sceneId = sceneId,
                    settings = SceneControlSettings(
                        brightness = state.brightness,
                        darkness = state.darkness,
                        timerMode = state.timerMode,
                        timerDurationMillis = state.timerDurationMillis,
                        showCountdownClock = state.showCountdownClock,
                        alarmReminderEnabled = state.alarmReminderEnabled &&
                            state.timerMode == SceneTimerMode.Countdown,
                        countdownClockPosition = state.countdownClockPosition,
                        clockAudioVolume = state.clockAudioVolume,
                        particle = state.particleSettings,
                        guideCompleted = guideCompleted,
                    ),
                )
            }
        }
    }

    private var timerJob: Job? = null

    /**
     * 统一取消当前定时器任务。
     * 所有模式切换、播放暂停、倒计时结束时都应先调用此方法。
     */
    private fun cancelTimerJob() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun startCountdown() {
        cancelTimerJob()
        timerJob = viewModelScope.launch {
            while (isActive) {
                val now = sceneControlClock.currentTimeMillis()
                _uiState.update {
                    it.copy(
                        currentTimestampMillis = now,
                        timerTick = it.timerTick + 1,
                    )
                }
                if (_uiState.value.timerRemainingMillis <= 0) break
                delay(500)
            }
            // 倒计时结束，通知上层（scene 负责停止播放等）
            if (isActive) {
                _uiState.update {
                    it.copy(
                        isTimerRunning = false,
                        pausedRemainingMillis = 0L,
                        timerEndTimestampMillis = 0L,
                    )
                }
                _events.send(ControlEvent.TimerExpired)
            }
        }
    }

    private fun startClockTicker() {
        cancelTimerJob()
        timerJob = viewModelScope.launch {
            while (isActive) {
                _uiState.update {
                    it.copy(
                        currentTimestampMillis = sceneControlClock.currentTimeMillis(),
                        timerTick = it.timerTick + 1,
                    )
                }
                delay(1000)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        cancelTimerJob()
    }
}

fun Long.formatTimerText(): String {
    val totalSeconds = (this / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

private fun Long.toTimerOptionIndex(): Int = TIMER_DURATIONS.indexOf(this).takeIf { it >= 0 } ?: 3
