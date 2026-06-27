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
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import androidx.core.content.getSystemService

/**
 * 音频焦点控制器，负责管理音频焦点请求与释放。
 *
 * 当其他应用需要使用音频时（如来电、导航语音），系统会请求音频焦点。
 * 通过回调通知上层应用进行相应的暂停、音量降低（duck）等操作，
 * 从而实现与其他音频应用的良好共存。
 *
 * @param context Android 上下文
 * @param onFocusGained 获取音频焦点时的回调，通常恢复播放
 * @param onFocusLost 永久失去音频焦点时的回调，通常暂停播放
 * @param onFocusLostTransient 暂时失去音频焦点时的回调（如来电），通常暂停播放
 * @param onDuck 收到"duck"提示时的回调，此时应降低音量而非完全暂停
 */
internal class AudioFocusController(
    context: Context,
    private val onFocusGained: () -> Unit = {},
    private val onFocusLost: () -> Unit = {},
    private val onFocusLostTransient: () -> Unit = onFocusLost,
    private val onDuck: () -> Unit = {},
) {
    private val audioManager =
        requireNotNull(context.applicationContext.getSystemService<AudioManager>()) {
            "AudioManager is unavailable."
        }

    /** 当前是否持有音频焦点 */
    private var hasAudioFocus = false

    /** 音频焦点变化监听器，根据不同的焦点状态触发对应回调 */
    private val focusChangeListener =
        AudioManager.OnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                // 永久获得音频焦点（如其他应用释放），恢复播放并恢复音量
                AudioManager.AUDIOFOCUS_GAIN -> {
                    hasAudioFocus = true
                    onFocusGained()
                }

                // 永久失去音频焦点（如其他应用开始播放），停止播放
                AudioManager.AUDIOFOCUS_LOSS -> {
                    hasAudioFocus = false
                    onFocusLost()
                }

                // 暂时失去音频焦点（如来电），暂停播放但保留播放位置
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    hasAudioFocus = false
                    onFocusLostTransient()
                }

                // 暂时失去焦点但可以继续播放（"duck"），此时应降低音量
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> onDuck()
            }
        }

    /**
     * 在 Android O（API 26）及以上版本使用 AudioFocusRequest，
     * 在更低版本使用废弃的 requestAudioFocus API。
     * 提前构建请求对象以避免每次请求时重复创建。
     */
    private val focusRequest: AudioFocusRequest? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                        .build(),
                )
                .setOnAudioFocusChangeListener(focusChangeListener)
                .build()
        } else {
            null
        }

    /**
     * 请求音频焦点。
     * @return 是否成功获取音频焦点
     */
    fun request(): Boolean {
        val result =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioManager.requestAudioFocus(checkNotNull(focusRequest))
            } else {
                @Suppress("DEPRECATION")
                audioManager.requestAudioFocus(
                    focusChangeListener,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN,
                )
            }
        hasAudioFocus = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        return hasAudioFocus
    }

    /**
     * 释放音频焦点。当不再需要播放音频时应调用此方法，
     * 以便其他应用可以获取焦点。
     */
    fun abandon() {
        if (!hasAudioFocus) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.abandonAudioFocusRequest(checkNotNull(focusRequest))
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(focusChangeListener)
        }
        hasAudioFocus = false
    }
}
