package com.wujia.foundation.toolkit.display

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import com.wujia.foundation.toolkit.app.AppContext

/**
 * Toast 工具类
 *
 * 提供 Toast 的便捷显示，支持短/长时长，支持字符串和资源 ID，
 * 每次显示前会取消上一个 Toast，避免队列堆积。
 * 默认使用 [AppContext.app] 作为 Context。
 */
object ToastUtils {

    private var toast: Toast? = null

    /**
     * 显示短时长 Toast
     *
     * @param text    要显示的文本
     * @param context 上下文，默认使用 AppContext
     */
    fun showShort(text: CharSequence, context: Context = AppContext.app) {
        cancel()
        toast = Toast.makeText(context.applicationContext, text, Toast.LENGTH_SHORT).also {
            it.show()
        }
    }

    /**
     * 显示短时长 Toast
     *
     * @param resId   字符串资源 ID
     * @param context 上下文，默认使用 AppContext
     */
    fun showShort(@StringRes resId: Int, context: Context = AppContext.app) {
        cancel()
        toast = Toast.makeText(context.applicationContext, resId, Toast.LENGTH_SHORT).also {
            it.show()
        }
    }

    /**
     * 显示长时长 Toast
     *
     * @param text    要显示的文本
     * @param context 上下文，默认使用 AppContext
     */
    fun showLong(text: CharSequence, context: Context = AppContext.app) {
        cancel()
        toast = Toast.makeText(context.applicationContext, text, Toast.LENGTH_LONG).also {
            it.show()
        }
    }

    /**
     * 显示长时长 Toast
     *
     * @param resId   字符串资源 ID
     * @param context 上下文，默认使用 AppContext
     */
    fun showLong(@StringRes resId: Int, context: Context = AppContext.app) {
        cancel()
        toast = Toast.makeText(context.applicationContext, resId, Toast.LENGTH_LONG).also {
            it.show()
        }
    }

    /**
     * 取消当前正在显示的 Toast
     */
    fun cancel() {
        toast?.cancel()
        toast = null
    }
}