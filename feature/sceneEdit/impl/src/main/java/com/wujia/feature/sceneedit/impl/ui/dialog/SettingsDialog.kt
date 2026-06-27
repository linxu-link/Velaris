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
package com.wujia.feature.sceneedit.impl.ui.dialog

import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.wujia.foundation.designsystem.theme.ProvideVelarisDialogVisuals
import com.wujia.foundation.designsystem.theme.VelarisTheme
import com.wujia.foundation.ui.VelarisDialogPanel

@Composable
internal fun SceneEditDialog(
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
    ) {
        ProvideVelarisDialogVisuals {
            val spec = VelarisTheme.spec
            VelarisDialogPanel(
                modifier = Modifier.widthIn(max = spec.size.dialogWidth),
                scrollable = true,
            ) {
                content()
            }
        }
    }
}
