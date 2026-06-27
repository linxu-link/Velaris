package com.wujia.foundation.toolkit.res

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.ArrayRes
import androidx.annotation.BoolRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.IntegerRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.wujia.foundation.toolkit.app.AppContext

/**
 * 资源工具类
 *
 * 提供各种资源类型的便捷获取，包括字符串、颜色、Drawable、尺寸等，
 * 兼容不同 API 级别，并提供夜间模式和 RTL 布局判断。
 * 默认使用 [AppContext.app] 作为 Context。
 */
object ResUtils {

    /**
     * 获取字符串资源
     *
     * @param resId   字符串资源 ID
     * @param context 上下文，默认使用 AppContext
     * @return 字符串值
     */
    fun getString(@StringRes resId: Int, context: Context = AppContext.app): String {
        return context.getString(resId)
    }

    /**
     * 获取带格式化参数的字符串资源
     *
     * @param resId       字符串资源 ID
     * @param formatArgs  格式化参数
     * @param context     上下文，默认使用 AppContext
     * @return 格式化后的字符串
     */
    fun getString(@StringRes resId: Int, vararg formatArgs: Any, context: Context = AppContext.app): String {
        return context.getString(resId, *formatArgs)
    }

    /**
     * 获取字符串数组资源
     *
     * @param resId   数组资源 ID
     * @param context 上下文，默认使用 AppContext
     * @return 字符串数组
     */
    fun getStringArray(@ArrayRes resId: Int, context: Context = AppContext.app): Array<String> {
        return context.resources.getStringArray(resId)
    }

    /**
     * 获取整数资源
     *
     * @param resId   整数资源 ID
     * @param context 上下文，默认使用 AppContext
     * @return 整数值
     */
    fun getInt(@IntegerRes resId: Int, context: Context = AppContext.app): Int {
        return context.resources.getInteger(resId)
    }

    /**
     * 获取整数数组资源
     *
     * @param resId   数组资源 ID
     * @param context 上下文，默认使用 AppContext
     * @return 整数数组
     */
    fun getIntArray(@ArrayRes resId: Int, context: Context = AppContext.app): IntArray {
        return context.resources.getIntArray(resId)
    }

    /**
     * 获取布尔值资源
     *
     * @param resId   布尔资源 ID
     * @param context 上下文，默认使用 AppContext
     * @return 布尔值
     */
    fun getBoolean(@BoolRes resId: Int, context: Context = AppContext.app): Boolean {
        return context.resources.getBoolean(resId)
    }

    /**
     * 获取颜色值，兼容各 API 级别
     *
     * @param resId   颜色资源 ID
     * @param context 上下文，默认使用 AppContext
     * @return 颜色值（ARGB 整型）
     */
    @ColorInt
    fun getColor(@ColorRes resId: Int, context: Context = AppContext.app): Int {
        return ContextCompat.getColor(context, resId)
    }

    /**
     * 获取颜色状态列表
     *
     * @param resId   颜色资源 ID
     * @param context 上下文，默认使用 AppContext
     * @return ColorStateList，资源不存在时返回 null
     */
    fun getColorStateList(@ColorRes resId: Int, context: Context = AppContext.app): ColorStateList? {
        return ContextCompat.getColorStateList(context, resId)
    }

    /**
     * 获取 Drawable 资源，兼容各 API 级别
     *
     * @param resId   Drawable 资源 ID
     * @param context 上下文，默认使用 AppContext
     * @return Drawable 对象，资源不存在时返回 null
     */
    fun getDrawable(@DrawableRes resId: Int, context: Context = AppContext.app): Drawable? {
        return ContextCompat.getDrawable(context, resId)
    }

    /**
     * 获取尺寸值（浮点型）
     *
     * @param resId   尺寸资源 ID
     * @param context 上下文，默认使用 AppContext
     * @return 尺寸值（px，浮点型）
     */
    fun getDimension(@DimenRes resId: Int, context: Context = AppContext.app): Float {
        return context.resources.getDimension(resId)
    }

    /**
     * 获取尺寸值（整型偏移），直接截断小数部分
     *
     * @param resId   尺寸资源 ID
     * @param context 上下文，默认使用 AppContext
     * @return 尺寸值（px，整型）
     */
    fun getDimensionPixelOffset(@DimenRes resId: Int, context: Context = AppContext.app): Int {
        return context.resources.getDimensionPixelOffset(resId)
    }

    /**
     * 获取尺寸值（整型），四舍五入
     *
     * @param resId   尺寸资源 ID
     * @param context 上下文，默认使用 AppContext
     * @return 尺寸值（px，整型，四舍五入）
     */
    fun getDimensionPixelSize(@DimenRes resId: Int, context: Context = AppContext.app): Int {
        return context.resources.getDimensionPixelSize(resId)
    }

    /**
     * 判断当前是否为夜间模式
     *
     * @param context 上下文，默认使用 AppContext
     * @return 夜间模式返回 true，否则返回 false
     */
    fun isNightMode(context: Context = AppContext.app): Boolean {
        val currentNightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }

    /**
     * 判断当前布局方向是否为 RTL（从右到左）
     *
     * @param context 上下文，默认使用 AppContext
     * @return RTL 布局返回 true，否则返回 false
     */
    fun isRtl(context: Context = AppContext.app): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            context.resources.configuration.layoutDirection == Configuration.SCREENLAYOUT_LAYOUTDIR_RTL
        } else {
            false
        }
    }
}