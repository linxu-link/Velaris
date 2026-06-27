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
 * 激励广告使用记录存储接口
 *
 * 持久化激励广告的使用数据（每日奖励次数、最后奖励时间），
 * 供 [RewardedAdGatekeeper] 进行频率限制评估。
 */
interface RewardedAdUsageStore {
    /** 获取指定场景的使用记录 */
    suspend fun getUsage(placement: RewardedAdPlacement): RewardedAdUsage

    /** 记录一次奖励获取 */
    suspend fun recordRewardEarned(placement: RewardedAdPlacement, nowMillis: Long)

    /** 仅调试用：清除所有使用记录 */
    suspend fun clearDebugOnly()
}
