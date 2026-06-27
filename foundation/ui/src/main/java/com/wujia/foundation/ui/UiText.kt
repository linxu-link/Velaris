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
package com.wujia.foundation.ui

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

sealed interface UiText {
    data class DynamicString(val value: String) : UiText

    data class StringResource(@StringRes val resId: Int, val args: List<Any> = emptyList()) : UiText
}

fun UiText.resolve(context: Context): String = when (this) {
    is UiText.DynamicString -> value
    is UiText.StringResource -> context.getString(resId, *args.toTypedArray())
}

@Composable
fun UiText.resolve(): String = resolve(LocalContext.current)
