package com.wujia.foundation.toolkit.process

import android.app.ActivityManager
import android.content.Context
import android.os.Process
import com.wujia.foundation.toolkit.app.AppContext

/**
 * 进程工具类
 *
 * 提供进程名获取、主进程判断、前后台判断、
 * 进程 ID 获取、后台进程清理等功能。
 * 默认使用 [AppContext.app] 作为 Context。
 */
object ProcessUtils {

    /**
     * 获取当前进程名
     *
     * @param context 上下文，默认使用 AppContext
     * @return 当前进程名，获取失败返回空字符串
     */
    fun getProcessName(context: Context = AppContext.app): String {
        val pid = Process.myPid()
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val processInfos = am.runningAppProcesses ?: return ""
        for (info in processInfos) {
            if (info.pid == pid) {
                return info.processName
            }
        }
        return ""
    }

    /**
     * 判断当前是否为主进程
     *
     * @param context 上下文，默认使用 AppContext
     * @return 主进程返回 true，否则返回 false
     */
    fun isMainProcess(context: Context = AppContext.app): Boolean {
        return context.packageName == getProcessName(context)
    }

    /**
     * 获取当前进程 PID
     *
     * @return 进程 ID
     */
    fun getCurrentPid(): Int {
        return Process.myPid()
    }

    /**
     * 获取当前进程 UID
     *
     * @return 用户 ID
     */
    fun getCurrentUid(): Int {
        return Process.myUid()
    }

    /**
     * 获取当前线程 TID
     *
     * @return 线程 ID
     */
    fun getCurrentTid(): Int {
        return Process.myTid()
    }

    /**
     * 获取所有正在运行的应用进程信息
     *
     * @param context 上下文，默认使用 AppContext
     * @return 运行中的进程列表，获取失败返回空列表
     */
    fun getRunningAppProcesses(context: Context = AppContext.app): List<ActivityManager.RunningAppProcessInfo> {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return am.runningAppProcesses ?: emptyList()
    }

    /**
     * 判断当前应用是否在前台运行
     *
     * @param context 上下文，默认使用 AppContext
     * @return 前台运行返回 true，否则返回 false
     */
    fun isAppForeground(context: Context = AppContext.app): Boolean {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val processInfos = am.runningAppProcesses ?: return false
        for (info in processInfos) {
            if (info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return info.processName == context.packageName
            }
        }
        return false
    }

    /**
     * 杀死指定包名的后台进程
     *
     * 注意：只能杀死后台进程，不能杀死前台进程
     *
     * @param packageName 要杀死的进程包名
     * @param context     上下文，默认使用 AppContext
     */
    fun killBackgroundProcesses(packageName: String, context: Context = AppContext.app) {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        am.killBackgroundProcesses(packageName)
    }
}