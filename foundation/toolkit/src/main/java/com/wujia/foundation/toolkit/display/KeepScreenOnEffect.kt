package com.wujia.foundation.toolkit.display

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

/**
 * 控制当前 Activity 窗口的息屏标志。
 *
 * 当 [keepScreenOn] 为 true 时，添加 [WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON]
 * 使屏幕保持常亮；为 false 时移除该标志，允许系统正常息屏。
 *
 * Composable 离开组合时自动移除标志，防止泄漏。
 *
 * @param keepScreenOn 是否保持屏幕常亮
 */
@Composable
fun KeepScreenOnEffect(
    keepScreenOn: Boolean,
) {
    val view = LocalView.current
    val window = remember(view) { view.context.findActivity()?.window }

    DisposableEffect(window) {
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    LaunchedEffect(window, keepScreenOn) {
        if (window == null) return@LaunchedEffect
        if (keepScreenOn) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
