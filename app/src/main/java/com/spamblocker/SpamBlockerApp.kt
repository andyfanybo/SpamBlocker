package com.spamblocker

import android.app.Application
import com.spamblocker.data.AppDatabase

/**
 * Application 类，初始化全局资源
 */
class SpamBlockerApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: SpamBlockerApp
            private set
    }
}
