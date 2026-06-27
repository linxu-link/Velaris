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

/**
 * 激励广告展示场景枚举
 *
 * 定义应用中需要展示激励广告的功能入口。
 */
enum class RewardedAdPlacement {
    /** 保存新创建的场景时 */
    NEW_SCENE_SAVE,
}

/**
 * 激励广告展示结果密封接口
 *
 * 表示用户与激励广告交互的七种可能结果。
 */
sealed interface RewardedAdResult {
    /** 用户完整观看广告并获得奖励 */
    data object RewardEarned : RewardedAdResult

    /** 跳过：上次获得奖励未超过冷却期 */
    data object SkippedRecentReward : RewardedAdResult

    /** 跳过：当日奖励次数已达上限 */
    data object SkippedDailyLimitReached : RewardedAdResult

    /** 跳过：广告不可用（SDK 未初始化或被禁用） */
    data object SkippedAdsUnavailable : RewardedAdResult

    /** 用户主动取消广告 */
    data object CancelledByUser : RewardedAdResult

    /** 广告关闭但用户未获得奖励 */
    data object ClosedWithoutReward : RewardedAdResult

    /**
     * 广告加载或展示过程中发生错误
     *
     * @property error 错误详情
     */
    data class Failure(val error: AdsError) : RewardedAdResult
}

/**
 * 激励广告门控状态密封接口
 *
 * 表示 [RewardedAdGatekeeper.evaluate] 的评估结果，
 * 决定是否需要展示激励广告或因何种原因跳过。
 */
sealed interface RewardedAdGateState {
    /** 需要展示广告 */
    data object Required : RewardedAdGateState

    /** 跳过：上次获得奖励未超过冷却期 */
    data object SkippedRecentReward : RewardedAdGateState

    /** 跳过：当日奖励次数已达上限 */
    data object SkippedDailyLimitReached : RewardedAdGateState

    /** 跳过：广告功能不可用 */
    data object SkippedAdsUnavailable : RewardedAdGateState
}

/**
 * 激励广告使用记录
 *
 * 用于频率限制的持久化数据。
 *
 * @property localDate 本地日期字符串（yyyy-MM-dd），用于判断每日计数是否需要重置
 * @property dailyRewardCount 当日已获得奖励次数
 * @property lastRewardEarnedAtMillis 上次获得奖励的时间戳（毫秒）
 */
data class RewardedAdUsage(val localDate: String, val dailyRewardCount: Int, val lastRewardEarnedAtMillis: Long)
