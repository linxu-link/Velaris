package com.wujia.foundation.toolkit.display

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

/**
 * Applies window-level brightness to the current Activity window.
 *
 * This only changes [android.view.WindowManager.LayoutParams.screenBrightness]
 * for the current window and does not write system brightness settings.
 */
@Composable
fun WindowBrightnessEffect(
    brightness: Float,
    restoreOnDispose: Boolean = true,
) {
    val view = LocalView.current
    val window = remember(view) { view.context.findActivity()?.window }

    DisposableEffect(window, restoreOnDispose) {
        val originalBrightness = window?.attributes?.screenBrightness
        onDispose {
            if (restoreOnDispose && window != null && originalBrightness != null) {
                BrightnessUtils.setRawWindowBrightness(
                    window = window,
                    brightness = originalBrightness,
                )
            }
        }
    }

    LaunchedEffect(window, brightness) {
        window?.let {
            BrightnessUtils.setWindowBrightness(
                window = it,
                brightness = brightness,
            )
        }
    }
}

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
