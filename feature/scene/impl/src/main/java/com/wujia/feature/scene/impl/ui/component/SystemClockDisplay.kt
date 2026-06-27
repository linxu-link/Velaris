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
package com.wujia.feature.scene.impl.ui.component

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.wujia.foundation.designsystem.clock.FlipClockTime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Immutable
internal data class SystemClockDisplay(val text: String, val flipTime: FlipClockTime)

@Composable
internal fun rememberSystemClockDisplay(timestampMillis: Long): SystemClockDisplay {
    val context = LocalContext.current
    val is24Hour = DateFormat.is24HourFormat(context)
    val locale = Locale.getDefault()
    return remember(timestampMillis, is24Hour, locale) {
        val date = Date(timestampMillis)
        val calendar = java.util.Calendar.getInstance(locale).apply {
            time = date
        }
        val hourOfDay = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val hour = if (is24Hour) {
            hourOfDay
        } else {
            val hour12 = calendar.get(java.util.Calendar.HOUR)
            if (hour12 == 0) 12 else hour12
        }
        SystemClockDisplay(
            text = SimpleDateFormat(
                if (is24Hour) "HH:mm:ss" else "h:mm:ss a",
                locale,
            ).format(date),
            flipTime = FlipClockTime(
                hours = hour,
                minutes = calendar.get(java.util.Calendar.MINUTE),
                seconds = calendar.get(java.util.Calendar.SECOND),
            ),
        )
    }
}
