/*
 * Copyright 2026 WuJia(Linxu_Link)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wujia.velaris

import android.app.Application
import android.content.pm.ApplicationInfo
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy.Builder
import android.util.Log
import com.wujia.foundation.toolkit.HiToolKit
import com.wujia.velaris.sync.initializers.Sync
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

private const val TAG = "[Velaris]"

@HiltAndroidApp
class VelarisApplication : Application() {

    private var isFirstActivityLaunchInProcess = true

    override fun onCreate() {
        super.onCreate()
        plantTimber()
        Timber.d("VelarisApplication onCreate")
        initHiToolKit()
        setStrictModePolicy()
        Sync.initialize(this)
    }

    /**
     * Return true if the application is debuggable.
     */
    private fun isDebuggable(): Boolean = 0 != applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE

    /**
     * Set a thread policy that detects all potential problems on the main thread, such as network
     * and disk access.
     *
     * If a problem is found, the offending call will be logged and the application will be killed.
     */
    private fun setStrictModePolicy() {
        if (isDebuggable()) {
            StrictMode.setThreadPolicy(
                Builder().detectAll().penaltyLog().build(),
            )
        }
    }

    private fun plantTimber() {
        val tree = if (isDebuggable()) {
            PrefixDebugTree(TAG)
        } else {
            PrefixReleaseTree(TAG)
        }
        Timber.plant(tree)
    }

    private fun initHiToolKit() {
        HiToolKit.init(this)
    }

    @Synchronized
    fun consumeColdStartLaunch(): Boolean {
        if (!isFirstActivityLaunchInProcess) return false
        isFirstActivityLaunchInProcess = false
        return true
    }
}

class PrefixDebugTree(private val prefix: String) : Timber.DebugTree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        super.log(priority, "$prefix $tag", message, t)
    }
}

class PrefixReleaseTree(private val prefix: String) : Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority < Log.WARN) return

        val resolvedTag = listOfNotNull(prefix, tag).joinToString(" ")
        if (t != null) {
            Log.println(priority, resolvedTag, "$message\n${Log.getStackTraceString(t)}")
        } else {
            Log.println(priority, resolvedTag, message)
        }
    }
}
