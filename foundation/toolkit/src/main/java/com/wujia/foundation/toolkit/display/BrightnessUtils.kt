package com.wujia.foundation.toolkit.display

import android.content.Context
import android.provider.Settings
import android.view.Window
import com.wujia.foundation.toolkit.app.AppContext

/**
 * 屏幕亮度工具类
 *
 * 提供系统亮度的读写、自动亮度模式开关、
 * 窗口级别亮度的设置与获取等功能。
 * 默认使用 [AppContext.app] 作为 Context。
 */
object BrightnessUtils {

    /**
     * 获取系统当前屏幕亮度
     *
     * @param context 上下文，默认使用 AppContext
     * @return 亮度值，范围 0~255
     */
    fun getSystemBrightness(context: Context = AppContext.app): Int {
        return Settings.System.getInt(
            context.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS,
            0
        )
    }

    /**
     * 设置系统屏幕亮度
     *
     * 注意：需要 WRITE_SETTINGS 权限，且需开启系统设置修改权限
     *
     * @param brightness 亮度值，范围 0~255，超出范围会自动裁剪
     * @param context    上下文，默认使用 AppContext
     */
    fun setSystemBrightness(brightness: Int, context: Context = AppContext.app) {
        val value = brightness.coerceIn(0, 255)
        Settings.System.putInt(
            context.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS,
            value
        )
    }

    /**
     * 判断是否开启了自动亮度调节
     *
     * @param context 上下文，默认使用 AppContext
     * @return 开启自动亮度返回 true，否则返回 false
     */
    fun isAutoBrightness(context: Context = AppContext.app): Boolean {
        return Settings.System.getInt(
            context.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS_MODE,
            Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
        ) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
    }

    /**
     * 设置自动亮度调节开关
     *
     * 注意：需要 WRITE_SETTINGS 权限
     *
     * @param enabled true 开启自动亮度，false 关闭
     * @param context 上下文，默认使用 AppContext
     */
    fun setAutoBrightness(enabled: Boolean, context: Context = AppContext.app) {
        Settings.System.putInt(
            context.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS_MODE,
            if (enabled) Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
            else Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
        )
    }

    /**
     * 设置窗口级别的屏幕亮度，仅影响当前 Window，不改变系统设置
     *
     * @param window     目标窗口
     * @param brightness 亮度值，范围 0.0~1.0，超出范围会自动裁剪
     */
    fun setWindowBrightness(window: Window, brightness: Float) {
        val value = brightness.coerceIn(0f, 1f)
        setRawWindowBrightness(window, value)
    }

    internal fun setRawWindowBrightness(window: Window, brightness: Float) {
        val layoutParams = window.attributes
        layoutParams.screenBrightness = brightness
        window.attributes = layoutParams
    }

    /**
     * 获取窗口级别的屏幕亮度
     *
     * @param window 目标窗口
     * @return 亮度值，范围 0.0~1.0，未设置过时返回 0.5
     */
    fun getWindowBrightness(window: Window): Float {
        return window.attributes.screenBrightness.let {
            if (it < 0) 0.5f else it
        }
    }
}
