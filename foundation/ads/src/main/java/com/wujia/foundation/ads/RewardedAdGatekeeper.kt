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
package com.wujia.foundation.ads

import android.app.Activity
import com.wujia.foundation.ads.rewarded.RewardedAdClock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 激励广告门控器接口
 *
 * 在展示激励广告前进行频率限制评估，
 * 根据冷却期和每日上限决定是否允许展示广告。
 */
interface RewardedAdGatekeeper {
    /**
     * 评估当前是否需要展示激励广告
     *
     * @return [RewardedAdGateState.Required] 表示需要展示，其他值表示因某种原因跳过
     */
    suspend fun evaluate(placement: RewardedAdPlacement): RewardedAdGateState

    /**
     * 按需展示激励广告
     *
     * 先评估门控状态，通过时展示广告并在获得奖励后记录使用情况。
     * 未通过时直接返回对应的跳过结果。
     */
    suspend fun showIfRequired(activity: Activity, placement: RewardedAdPlacement): RewardedAdResult
}

/**
 * 默认激励广告门控器实现
 *
 * 在展示激励广告前执行频率限制策略：
 * - **冷却期**：上次获得奖励后 [RewardedAdConfig.hourlyCooldownMillis] 内不再展示广告
 * - **每日上限**：当日奖励次数达到 [RewardedAdConfig.dailyRewardLimit] 后不再展示广告
 *
 * 当门控通过时，委托 [RewardedAdManager] 展示广告；
 * 展示成功获得奖励后，通过 [RewardedAdUsageStore] 记录使用情况。
 */
@Singleton
internal class DefaultRewardedAdGatekeeper @Inject constructor(
    private val configProvider: AdsConfigProvider,
    private val usageStore: RewardedAdUsageStore,
    private val rewardedAdManager: RewardedAdManager,
    private val clock: RewardedAdClock,
) : RewardedAdGatekeeper {
    override suspend fun evaluate(placement: RewardedAdPlacement): RewardedAdGateState {
        val config = configProvider.getConfig()
        if (!config.enabled) return RewardedAdGateState.SkippedAdsUnavailable

        val usage = usageStore.getUsage(placement)
        val nowMillis = clock.nowMillis()
        if (usage.lastRewardEarnedAtMillis > 0 &&
            nowMillis - usage.lastRewardEarnedAtMillis < config.rewardedAdConfig.hourlyCooldownMillis
        ) {
            return RewardedAdGateState.SkippedRecentReward
        }
        if (usage.dailyRewardCount >= config.rewardedAdConfig.dailyRewardLimit) {
            return RewardedAdGateState.SkippedDailyLimitReached
        }
        return RewardedAdGateState.Required
    }

    override suspend fun showIfRequired(activity: Activity, placement: RewardedAdPlacement): RewardedAdResult =
        when (evaluate(placement)) {
            RewardedAdGateState.Required -> {
                val result = rewardedAdManager.show(activity, placement)
                if (result == RewardedAdResult.RewardEarned) {
                    usageStore.recordRewardEarned(placement, clock.nowMillis())
                }
                result
            }
            RewardedAdGateState.SkippedRecentReward -> RewardedAdResult.SkippedRecentReward
            RewardedAdGateState.SkippedDailyLimitReached -> RewardedAdResult.SkippedDailyLimitReached
            RewardedAdGateState.SkippedAdsUnavailable -> RewardedAdResult.SkippedAdsUnavailable
        }
}
