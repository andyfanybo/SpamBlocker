package com.spamblocker.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * 开机自启动接收器
 * 确保设备重启后服务仍然有效
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.i(TAG, "设备启动完成，骚扰拦截服务就绪")
            // CallScreeningService 和 SmsBlockReceiver 已在 Manifest 中注册，
            // 系统会自动绑定，此处仅做日志记录
        }
    }
}
