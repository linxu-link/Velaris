package com.wujia.foundation.toolkit.display

import android.content.Context
import android.graphics.Point
import android.os.Build
import android.view.WindowManager
import com.wujia.foundation.toolkit.app.AppContext

/**
 * 屏幕信息工具类
 *
 * 提供屏幕宽高、密度、状态栏/导航栏高度、横竖屏判断等屏幕相关信息获取。
 * 默认使用 [AppContext.app] 作为 Context。
 */
object ScreenUtils {

    /**
     * 获取屏幕实际宽度（px），包含系统装饰区域
     *
     * @param context 上下文，默认使用 AppContext
     * @return 屏幕宽度（px）
     */
    fun getScreenWidth(context: Context = AppContext.app): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val point = Point()
        wm.defaultDisplay.getRealSize(point)
        return point.x
    }

    /**
     * 获取屏幕实际高度（px），包含系统装饰区域
     *
     * @param context 上下文，默认使用 AppContext
     * @return 屏幕高度（px）
     */
    fun getScreenHeight(context: Context = AppContext.app): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val point = Point()
        wm.defaultDisplay.getRealSize(point)
        return point.y
    }

    /**
     * 获取屏幕宽度（dp）
     *
     * @param context 上下文，默认使用 AppContext
     * @return 屏幕宽度（dp）
     */
    fun getScreenWidthDp(context: Context = AppContext.app): Int {
        val widthPx = getScreenWidth(context)
        val density = context.resources.displayMetrics.density
        return (widthPx / density).toInt()
    }

    /**
     * 获取屏幕高度（dp）
     *
     * @param context 上下文，默认使用 AppContext
     * @return 屏幕高度（dp）
     */
    fun getScreenHeightDp(context: Context = AppContext.app): Int {
        val heightPx = getScreenHeight(context)
        val density = context.resources.displayMetrics.density
        return (heightPx / density).toInt()
    }

    /**
     * 获取屏幕密度（density）
     *
     * @param context 上下文，默认使用 AppContext
     * @return 屏幕密度值，如 1.0、1.5、2.0、3.0 等
     */
    fun getScreenDensity(context: Context = AppContext.app): Float {
        return context.resources.displayMetrics.density
    }

    /**
     * 获取屏幕密度 DPI
     *
     * @param context 上下文，默认使用 AppContext
     * @return 屏幕 DPI 值，如 160、240、320、480 等
     */
    fun getScreenDensityDpi(context: Context = AppContext.app): Int {
        return context.resources.displayMetrics.densityDpi
    }

    /**
     * 获取状态栏高度（px）
     *
     * @param context 上下文，默认使用 AppContext
     * @return 状态栏高度（px），获取失败返回 0
     */
    fun getStatusBarHeight(context: Context = AppContext.app): Int {
        val resourceId = context.resources.getIdentifier(
            "status_bar_height", "dimen", "android"
        )
        return if (resourceId > 0) context.resources.getDimensionPixelSize(resourceId) else 0
    }

    /**
     * 获取导航栏高度（px）
     *
     * @param context 上下文，默认使用 AppContext
     * @return 导航栏高度（px），获取失败返回 0
     */
    fun getNavigationBarHeight(context: Context = AppContext.app): Int {
        val resourceId = context.resources.getIdentifier(
            "navigation_bar_height", "dimen", "android"
        )
        return if (resourceId > 0) context.resources.getDimensionPixelSize(resourceId) else 0
    }

    /**
     * 判断设备是否有导航栏
     *
     * 通过检测是否有永久菜单键和返回键来判断
     *
     * @param context 上下文，默认使用 AppContext
     * @return 有导航栏返回 true，否则返回 false
     */
    fun hasNavigationBar(context: Context = AppContext.app): Boolean {
        val hasMenuKey = android.view.ViewConfiguration.get(context).hasPermanentMenuKey()
        val hasBackKey = android.view.KeyCharacterMap.deviceHasKey(android.view.KeyEvent.KEYCODE_BACK)
        return !hasMenuKey && !hasBackKey
    }

    /**
     * 判断当前是否为横屏
     *
     * @param context 上下文，默认使用 AppContext
     * @return 横屏返回 true，否则返回 false
     */
    fun isLandscape(context: Context = AppContext.app): Boolean {
        return context.resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    }

    /**
     * 判断当前是否为竖屏
     *
     * @param context 上下文，默认使用 AppContext
     * @return 竖屏返回 true，否则返回 false
     */
    fun isPortrait(context: Context = AppContext.app): Boolean {
        return context.resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT
    }

    /**
     * 获取屏幕尺寸（应用窗口区域），Android R 及以上使用 WindowMetrics
     *
     * @param context 上下文，默认使用 AppContext
     * @return 屏幕尺寸 Point，x 为宽度，y 为高度（px）
     */
    fun getScreenSize(context: Context = AppContext.app): Point {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val point = Point()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = wm.currentWindowMetrics.bounds
            point.x = bounds.width()
            point.y = bounds.height()
        } else {
            @Suppress("DEPRECATION")
            wm.defaultDisplay.getRealSize(point)
        }
        return point
    }

    /**
     * 获取屏幕真实尺寸（包含系统装饰区域）
     *
     * @param context 上下文，默认使用 AppContext
     * @return 屏幕真实尺寸 Point，x 为宽度，y 为高度（px）
     */
    fun getRealScreenSize(context: Context = AppContext.app): Point {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val point = Point()
        wm.defaultDisplay.getRealSize(point)
        return point
    }
}