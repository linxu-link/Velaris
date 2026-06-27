package com.wujia.foundation.toolkit.app

import android.app.Application
import android.content.Context

/**
 * Application 上下文提供者
 *
 * 通过反射自动获取当前应用的 Application 实例并缓存，
 * 也支持通过 [init] 手动初始化。所有 toolkit 工具类默认使用此处的 Application 作为 Context。
 */
object AppContext {

    private var _app: Application? = null

    /**
     * 获取缓存的 Application 实例
     *
     * 如果未手动初始化，会通过反射自动获取
     *
     * @return Application 实例
     * @throws IllegalStateException 无法获取 Application 时抛出
     */
    val app: Application
        get() {
            _app?.let { return it }
            val instance = reflectApplication()
            _app = instance
            return instance
                ?: throw IllegalStateException(
                    "AppContext not initialized, please call HiToolKit.init(context) in Application.onCreate()"
                )
        }

    /**
     * 手动初始化，传入 Application 实例
     *
     * @param context Application 或任意 Context，内部取 applicationContext
     */
    fun init(context: Context) {
        _app = context.applicationContext as Application
    }

    /**
     * 是否已初始化
     */
    val isInitialized: Boolean
        get() = _app != null

    /**
     * 通过反射获取当前应用的 Application 实例
     */
    private fun reflectApplication(): Application? {
        return try {
            val activityThreadClass = Class.forName("android.app.ActivityThread")
            val currentActivityThread = activityThreadClass.getDeclaredMethod("currentActivityThread")
            currentActivityThread.invoke(null)?.let { activityThread ->
                val getApplication = activityThreadClass.getDeclaredMethod("getApplication")
                getApplication.invoke(activityThread) as? Application
            }
        } catch (_: Exception) {
            null
        }
    }
}