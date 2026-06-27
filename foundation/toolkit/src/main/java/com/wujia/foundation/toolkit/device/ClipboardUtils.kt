package com.wujia.foundation.toolkit.device

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.wujia.foundation.toolkit.app.AppContext

/**
 * 剪贴板工具类
 *
 * 提供剪贴板文本、URI、Intent 的复制与读取，
 * 以及剪贴板清空和内容判断等功能。
 * 默认使用 [AppContext.app] 作为 Context。
 */
object ClipboardUtils {

    /**
     * 复制文本到剪贴板
     *
     * @param text    要复制的文本
     * @param context 上下文，默认使用 AppContext
     */
    fun copyText(text: CharSequence, context: Context = AppContext.app) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("text", text))
    }

    /**
     * 从剪贴板读取文本
     *
     * @param context 上下文，默认使用 AppContext
     * @return 剪贴板中的文本，无内容时返回 null
     */
    fun getText(context: Context = AppContext.app): CharSequence? {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = cm.primaryClip ?: return null
        if (clip.itemCount > 0) {
            return clip.getItemAt(0).coerceToText(context)
        }
        return null
    }

    /**
     * 复制 URI 到剪贴板
     *
     * @param uri     要复制的 URI
     * @param context 上下文，默认使用 AppContext
     */
    fun copyUri(uri: Uri, context: Context = AppContext.app) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newUri(context.contentResolver, "uri", uri))
    }

    /**
     * 从剪贴板读取 URI
     *
     * @param context 上下文，默认使用 AppContext
     * @return 剪贴板中的 URI，无内容时返回 null
     */
    fun getUri(context: Context = AppContext.app): Uri? {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = cm.primaryClip ?: return null
        if (clip.itemCount > 0) {
            return clip.getItemAt(0).uri
        }
        return null
    }

    /**
     * 复制 Intent 到剪贴板
     *
     * @param intent  要复制的 Intent
     * @param context 上下文，默认使用 AppContext
     */
    fun copyIntent(intent: Intent, context: Context = AppContext.app) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newIntent("intent", intent))
    }

    /**
     * 从剪贴板读取 Intent
     *
     * @param context 上下文，默认使用 AppContext
     * @return 剪贴板中的 Intent，无内容时返回 null
     */
    fun getIntent(context: Context = AppContext.app): Intent? {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = cm.primaryClip ?: return null
        if (clip.itemCount > 0) {
            return clip.getItemAt(0).intent
        }
        return null
    }

    /**
     * 清空剪贴板内容
     *
     * @param context 上下文，默认使用 AppContext
     */
    fun clear(context: Context = AppContext.app) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText(null, ""))
    }

    /**
     * 判断剪贴板中是否有文本内容
     *
     * @param context 上下文，默认使用 AppContext
     * @return 有文本内容返回 true，否则返回 false
     */
    fun hasText(context: Context = AppContext.app): Boolean {
        val text = getText(context)
        return !text.isNullOrBlank()
    }
}