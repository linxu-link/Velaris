package com.wujia.foundation.toolkit.app

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build

/**
 * 应用信息工具类
 *
 * 提供应用版本信息、包名、名称、图标、Debug 状态、
 * 安装判断、SDK 版本、路径等常用应用信息的获取。
 * 默认使用 [AppContext.app] 作为 Context。
 */
object AppUtils {

    /**
     * 获取应用版本名称
     *
     * @param context 上下文，默认使用 AppContext
     * @return 版本名称字符串，如 "1.0.0"
     */
    fun getAppVersionName(context: Context = AppContext.app): String {
        return getPackageInfo(context)?.versionName ?: ""
    }

    /**
     * 获取应用版本号
     *
     * @param context 上下文，默认使用 AppContext
     * @return 版本号，Android P 及以上返回 longVersionCode，以下返回 versionCode
     */
    fun getAppVersionCode(context: Context = AppContext.app): Long {
        val packageInfo = getPackageInfo(context) ?: return 0L
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode.toLong()
        }
    }

    /**
     * 获取应用包名
     *
     * @param context 上下文，默认使用 AppContext
     * @return 包名字符串
     */
    fun getPackageName(context: Context = AppContext.app): String {
        return context.packageName
    }

    /**
     * 获取应用名称
     *
     * @param context 上下文，默认使用 AppContext
     * @return 应用名称字符串
     */
    fun getAppName(context: Context = AppContext.app): String {
        val packageManager = context.packageManager
        val applicationInfo = context.applicationInfo
        return packageManager.getApplicationLabel(applicationInfo).toString()
    }

    /**
     * 获取应用图标
     *
     * @param packageName 包名，默认为当前应用包名
     * @param context     上下文，默认使用 AppContext
     * @return 应用图标 Drawable，包名不存在时返回 null
     */
    fun getAppIcon(
        packageName: String? = null,
        context: Context = AppContext.app
    ): android.graphics.drawable.Drawable? {
        val pkg = packageName ?: context.packageName
        return try {
            context.packageManager.getApplicationIcon(pkg)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    /**
     * 判断当前应用是否为 Debug 模式
     *
     * @param context 上下文，默认使用 AppContext
     * @return Debug 模式返回 true，否则返回 false
     */
    fun isDebug(context: Context = AppContext.app): Boolean {
        return (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    /**
     * 判断当前应用是否为系统应用
     *
     * @param context 上下文，默认使用 AppContext
     * @return 系统应用返回 true，否则返回 false
     */
    fun isSystemApp(context: Context = AppContext.app): Boolean {
        return (context.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
    }

    /**
     * 判断指定包名的应用是否已安装
     *
     * @param packageName 要检查的包名
     * @param context     上下文，默认使用 AppContext
     * @return 已安装返回 true，否则返回 false
     */
    fun isAppInstalled(packageName: String, context: Context = AppContext.app): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * 获取应用的 targetSdkVersion
     *
     * @param context 上下文，默认使用 AppContext
     * @return targetSdkVersion 值
     */
    fun getTargetSdkVersion(context: Context = AppContext.app): Int {
        return context.applicationInfo.targetSdkVersion
    }

    /**
     * 获取应用的 minSdkVersion
     *
     * @param context 上下文，默认使用 AppContext
     * @return minSdkVersion 值，无法获取时返回 0
     */
    fun getMinSdkVersion(context: Context = AppContext.app): Int {
        return context.applicationInfo.minSdkVersion ?: 0
    }

    /**
     * 获取应用 APK 文件路径
     *
     * @param context 上下文，默认使用 AppContext
     * @return APK 文件路径
     */
    fun getAppPath(context: Context = AppContext.app): String {
        return context.applicationInfo.sourceDir
    }

    /**
     * 获取应用数据目录路径
     *
     * @param context 上下文，默认使用 AppContext
     * @return 数据目录路径
     */
    fun getDataDir(context: Context = AppContext.app): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.dataDir.absolutePath
        } else {
            context.applicationInfo.dataDir
        }
    }

    private fun getPackageInfo(context: Context): PackageInfo? {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }
}