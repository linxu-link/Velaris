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
package com.wujia.foundation.ads.rewarded

import android.content.Context
import android.content.SharedPreferences
import com.wujia.foundation.ads.RewardedAdPlacement
import com.wujia.foundation.ads.RewardedAdUsage
import com.wujia.foundation.ads.RewardedAdUsageStore
import com.wujia.foundation.toolkit.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 激励广告使用时钟接口
 *
 * 抽象系统时间获取，便于测试时注入固定时间。
 */
internal fun interface RewardedAdClock {
    fun nowMillis(): Long
}

/**
 * [RewardedAdClock] 的系统时钟实现
 */
internal class SystemRewardedAdClock @Inject constructor() : RewardedAdClock {
    override fun nowMillis(): Long = System.currentTimeMillis()
}

/**
 * 基于 SharedPreferences 的激励广告使用记录存储
 *
 * 将每日奖励次数和最后奖励时间持久化到 SharedPreferences 中。
 * 当检测到本地日期变更时，自动将每日计数重置为 0。
 *
 * 存储键命名规则：`rewarded_{placement}_{field}`
 */
@Singleton
internal class SharedPreferencesRewardedAdUsageStore @Inject constructor(
    @ApplicationContext context: Context,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val clock: RewardedAdClock,
) : RewardedAdUsageStore {
    private val preferences: SharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    override suspend fun getUsage(placement: RewardedAdPlacement): RewardedAdUsage = withContext(ioDispatcher) {
        val today = localDate(clock.nowMillis())
        val storedDate = preferences.getString(placement.dateKey(), null)
        RewardedAdUsage(
            localDate = today,
            dailyRewardCount = if (storedDate == today) {
                preferences.getInt(placement.countKey(), 0)
            } else {
                0
            },
            lastRewardEarnedAtMillis = preferences.getLong(placement.lastRewardKey(), 0L),
        )
    }

    override suspend fun recordRewardEarned(placement: RewardedAdPlacement, nowMillis: Long) {
        withContext(ioDispatcher) {
            val today = localDate(nowMillis)
            val usage = getUsageForDate(placement = placement, today = today)
            preferences.edit()
                .putString(placement.dateKey(), today)
                .putInt(placement.countKey(), usage.dailyRewardCount + 1)
                .putLong(placement.lastRewardKey(), nowMillis)
                .apply()
        }
    }

    override suspend fun clearDebugOnly() {
        withContext(ioDispatcher) {
            preferences.edit().clear().apply()
        }
    }

    private fun getUsageForDate(placement: RewardedAdPlacement, today: String): RewardedAdUsage {
        val storedDate = preferences.getString(placement.dateKey(), null)
        return RewardedAdUsage(
            localDate = today,
            dailyRewardCount = if (storedDate == today) {
                preferences.getInt(placement.countKey(), 0)
            } else {
                0
            },
            lastRewardEarnedAtMillis = preferences.getLong(placement.lastRewardKey(), 0L),
        )
    }

    private fun localDate(nowMillis: Long): String = dateFormat.format(Date(nowMillis))

    private fun RewardedAdPlacement.keyPrefix(): String = "rewarded_${name.lowercase()}"

    private fun RewardedAdPlacement.dateKey(): String = "${keyPrefix()}_date"

    private fun RewardedAdPlacement.countKey(): String = "${keyPrefix()}_count"

    private fun RewardedAdPlacement.lastRewardKey(): String = "${keyPrefix()}_last_reward"

    private companion object {
        const val PREFERENCES_NAME = "velaris_rewarded_ads"
    }
}
