package com.wujia.foundation.toolkit

import android.content.Context
import com.wujia.foundation.toolkit.app.AppContext
import com.wujia.foundation.toolkit.app.AppUtils
import com.wujia.foundation.toolkit.coroutine.CoroutineKit
import com.wujia.foundation.toolkit.device.ClipboardUtils
import com.wujia.foundation.toolkit.device.DeviceUtils
import com.wujia.foundation.toolkit.display.BrightnessUtils
import com.wujia.foundation.toolkit.display.DensityUtils
import com.wujia.foundation.toolkit.display.KeyboardUtils
import com.wujia.foundation.toolkit.display.ScreenUtils
import com.wujia.foundation.toolkit.display.ToastUtils
import com.wujia.foundation.toolkit.file.FileUtils
import com.wujia.foundation.toolkit.intent.IntentUtils
import com.wujia.foundation.toolkit.process.ProcessUtils
import com.wujia.foundation.toolkit.res.ResUtils
import com.wujia.foundation.toolkit.storage.SPUtils
import com.wujia.foundation.toolkit.time.TimeUtils

/**
 * Toolkit 统一入口
 *
 * 提供所有工具类的统一访问方式。
 * 建议在 Application.onCreate 中调用 [init] 初始化，
 * 未初始化时 [com.wujia.foundation.toolkit.app.AppContext] 会通过反射自动获取 Application。
 *
 * 示例：
 * ```kotlin
 * HiToolKit.init(application)
 * HiToolKit.app.getVersionName()
 * HiToolKit.screen.getWidth()
 * HiToolKit.storage.put("key", "value")
 * ```
 */
object HiToolKit {

    /**
     * 初始化 HiToolKit，建议在 Application.onCreate 中调用
     *
     * @param context 应用上下文
     */
    fun init(context: Context) {
        AppContext.init(context)
        SPUtils.init(AppContext.app)
    }

    /** 应用全局上下文 */
    val appContext: Context
        get() = AppContext.app

    /** 应用信息工具类 */
    val app = AppUtils

    /** 资源工具类 */
    val res = ResUtils

    /** 存储工具类（SharedPreferences） */
    val storage = SPUtils

    /** 尺寸密度转换工具类 */
    val density = DensityUtils

    /** 屏幕信息工具类 */
    val screen = ScreenUtils

    /** 软键盘工具类 */
    val keyboard = KeyboardUtils

    /** Toast 工具类 */
    val toast = ToastUtils

    /** 屏幕亮度工具类 */
    val brightness = BrightnessUtils

    /** 设备信息工具类 */
    val device = DeviceUtils

    /** 剪贴板工具类 */
    val clipboard = ClipboardUtils

    /** 时间日期工具类 */
    val time = TimeUtils

    /** 文件工具类 */
    val file = FileUtils

    /** Intent 工具类 */
    val intent = IntentUtils

    /** 进程工具类 */
    val process = ProcessUtils

    /**协程工具类 */
    val coroutine = CoroutineKit
}