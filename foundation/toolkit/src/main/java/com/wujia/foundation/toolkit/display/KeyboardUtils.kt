package com.wujia.foundation.toolkit.display

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.wujia.foundation.toolkit.app.AppContext

/**
 * 软键盘工具类
 *
 * 提供软键盘的显示、隐藏、切换以及状态判断等功能。
 * 默认使用 [AppContext.app] 作为 Context。
 */
object KeyboardUtils {

    /**
     * 显示软键盘，并让 EditText 获取焦点
     *
     * @param editText 需要弹出键盘的输入框
     */
    fun showSoftInput(editText: EditText) {
        editText.isFocusable = true
        editText.isFocusableInTouchMode = true
        editText.requestFocus()
        val imm = editText.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    /**
     * 隐藏软键盘
     *
     * @param view 当前获取焦点的 View
     */
    fun hideSoftInput(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    /**
     * 隐藏 Activity 中的软键盘，自动获取当前焦点 View
     *
     * @param activity 当前 Activity
     */
    fun hideSoftInput(activity: Activity) {
        val view = activity.currentFocus ?: return
        hideSoftInput(view)
    }

    /**
     * 切换软键盘的显示/隐藏状态
     *
     * @param context 上下文，默认使用 AppContext
     */
    fun toggleSoftInput(context: Context = AppContext.app) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(0, 0)
    }

    /**
     * 判断软键盘是否对指定 EditText 激活
     *
     * @param editText 输入框
     * @return 激活返回 true，否则返回 false
     */
    fun isActive(editText: EditText): Boolean {
        val imm = editText.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        return imm.isActive(editText)
    }
}