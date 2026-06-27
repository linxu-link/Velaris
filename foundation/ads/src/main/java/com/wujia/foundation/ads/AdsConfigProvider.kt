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
 * 广告配置提供者接口
 *
 * 为广告模块提供当前的 [AdsConfig] 实例。
 * 实现类通常从 BuildConfig 或远程配置中读取配置。
 */
interface AdsConfigProvider {
    fun getConfig(): AdsConfig
}
