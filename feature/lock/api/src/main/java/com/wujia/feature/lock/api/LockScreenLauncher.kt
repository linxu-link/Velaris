package com.wujia.feature.lock.api

import android.content.Context
import android.content.Intent

object LockScreenLauncher {

    private const val ACTIVITY_CLASS =
        "com.wujia.feature.lock.impl.ui.LockScreenActivity"

    fun createIntent(context: Context): Intent {
        return Intent().apply {
            setClassName(context.packageName, ACTIVITY_CLASS)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
