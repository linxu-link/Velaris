package com.wujia.foundation.toolkit.display

import android.content.Context
import android.util.TypedValue
import com.wujia.foundation.toolkit.app.AppContext

/**
 * 尺寸密度转换工具类
 *
 * 提供 dp、sp、pt、in、mm 等单位与 px 之间的互相转换，
 * 包含整型和浮点型返回值两种方式。默认使用 [AppContext.app] 作为 Context。
 */
object DensityUtils {

    /**
     * dp 值转为 px（整型）
     *
     * @param dpValue dp 值
     * @param context 上下文，默认使用 AppContext
     * @return 对应的 px 值（四舍五入取整）
     */
    fun dp2px(dpValue: Float, context: Context = AppContext.app): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    /**
     * px 值转为 dp（整型）
     *
     * @param pxValue px 值
     * @param context 上下文，默认使用 AppContext
     * @return 对应的 dp 值（四舍五入取整）
     */
    fun px2dp(pxValue: Float, context: Context = AppContext.app): Int {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

    /**
     * sp 值转为 px（整型）
     *
     * @param spValue sp 值
     * @param context 上下文，默认使用 AppContext
     * @return 对应的 px 值（四舍五入取整）
     */
    fun sp2px(spValue: Float, context: Context = AppContext.app): Int {
        val scale = context.resources.displayMetrics.scaledDensity
        return (spValue * scale + 0.5f).toInt()
    }

    /**
     * px 值转为 sp（整型）
     *
     * @param pxValue px 值
     * @param context 上下文，默认使用 AppContext
     * @return 对应的 sp 值（四舍五入取整）
     */
    fun px2sp(pxValue: Float, context: Context = AppContext.app): Int {
        val scale = context.resources.displayMetrics.scaledDensity
        return (pxValue / scale + 0.5f).toInt()
    }

    /**
     * dp 值转为 px（浮点型），使用 TypedValue 精确转换
     *
     * @param dpValue dp 值
     * @param context 上下文，默认使用 AppContext
     * @return 对应的 px 值（浮点型）
     */
    fun dp2pxF(dpValue: Float, context: Context = AppContext.app): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dpValue, context.resources.displayMetrics
        )
    }

    /**
     * sp 值转为 px（浮点型），使用 TypedValue 精确转换
     *
     * @param spValue sp 值
     * @param context 上下文，默认使用 AppContext
     * @return 对应的 px 值（浮点型）
     */
    fun sp2pxF(spValue: Float, context: Context = AppContext.app): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, spValue, context.resources.displayMetrics
        )
    }

    /**
     * pt 值转为 px（浮点型）
     *
     * @param ptValue pt 值
     * @param context 上下文，默认使用 AppContext
     * @return 对应的 px 值（浮点型）
     */
    fun pt2px(ptValue: Float, context: Context = AppContext.app): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_PT, ptValue, context.resources.displayMetrics
        )
    }

    /**
     * in（英寸）值转为 px（浮点型）
     *
     * @param inValue 英寸值
     * @param context 上下文，默认使用 AppContext
     * @return 对应的 px 值（浮点型）
     */
    fun in2px(inValue: Float, context: Context = AppContext.app): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_IN, inValue, context.resources.displayMetrics
        )
    }

    /**
     * mm（毫米）值转为 px（浮点型）
     *
     * @param mmValue 毫米值
     * @param context 上下文，默认使用 AppContext
     * @return 对应的 px 值（浮点型）
     */
    fun mm2px(mmValue: Float, context: Context = AppContext.app): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_MM, mmValue, context.resources.displayMetrics
        )
    }
}