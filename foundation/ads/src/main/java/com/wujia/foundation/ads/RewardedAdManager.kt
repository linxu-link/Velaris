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

/**
 * 激励广告管理器接口
 *
 * 负责激励广告的加载和展示。
 * 展示前会自动确保广告 SDK 已完成初始化（包括用户同意收集）。
 */
interface RewardedAdManager {
    /**
     * 展示激励广告
     *
     * @param activity 当前 Activity，用于展示广告和收集同意
     * @param placement 广告展示场景
     * @return 展示结果，参见 [RewardedAdResult]
     */
    suspend fun show(activity: Activity, placement: RewardedAdPlacement): RewardedAdResult
}
