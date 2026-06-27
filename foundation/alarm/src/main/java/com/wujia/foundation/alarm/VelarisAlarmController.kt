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
package com.wujia.foundation.alarm

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import androidx.core.net.toUri

/**
 * 倒计时闹钟控制器。
 *
 * 负责申请音频焦点、播放系统闹钟铃声，以及停止和释放资源。
 */
class VelarisAlarmController(context: Context) {
    private val appContext = context.applicationContext
    private val audioFocusController = AlarmAudioFocusController(
        context = appContext,
        onFocusLost = { stopInternal(abandonFocus = false) },
    )

    private var mediaPlayer: MediaPlayer? = null

    val isPlaying: Boolean
        get() = mediaPlayer?.isPlaying == true

    fun start(): Boolean {
        if (isPlaying) return true
        if (!audioFocusController.request()) return false

        val player = buildPlayer(resolveAlarmUri()) ?: run {
            audioFocusController.abandon()
            return false
        }

        mediaPlayer = player
        player.start()
        return true
    }

    fun stop() {
        stopInternal(abandonFocus = true)
    }

    fun release() {
        stopInternal(abandonFocus = true)
    }

    private fun stopInternal(abandonFocus: Boolean) {
        mediaPlayer?.run {
            runCatching {
                if (isPlaying) {
                    stop()
                }
            }
            runCatching { reset() }
            runCatching { release() }
        }
        mediaPlayer = null
        if (abandonFocus) {
            audioFocusController.abandon()
        }
    }

    private fun buildPlayer(uri: Uri): MediaPlayer? = runCatching {
        MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            setDataSource(appContext, uri)
            isLooping = true
            setOnErrorListener { _, _, _ ->
                stopInternal(abandonFocus = true)
                true
            }
            prepare()
        }
    }.getOrNull()

    private fun resolveAlarmUri(): Uri {
        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        if (alarmUri != null) return alarmUri

        val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        if (notificationUri != null) return notificationUri

        return RingtoneManager.getActualDefaultRingtoneUri(
            appContext,
            RingtoneManager.TYPE_ALARM,
        ) ?: RingtoneManager.getActualDefaultRingtoneUri(
            appContext,
            RingtoneManager.TYPE_NOTIFICATION,
        ) ?: "content://settings/system/alarm_alert".toUri()
    }
}
