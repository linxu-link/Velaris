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
package com.wujia.foundation.model.theme

/**
 * Velaris 全局主题预设。
 *
 * 这里只定义预设枚举，不绑定具体颜色资源，便于 data 层持久化和
 * design system 层做视觉映射。
 */
enum class VelarisThemePreset {
    Gold,
    Ocean,
    Forest,
    Twilight,
}
