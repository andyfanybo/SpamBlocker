package com.spamblocker.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.spamblocker.MainActivity
import com.spamblocker.R
import com.spamblocker.data.AppDatabase
import com.spamblocker.util.PhoneNumberUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * 来电筛查服务
 * 使用 Android CallScreeningService API 实现来电拦截
 * 适用于 Android 7.0+ (API 24+)，Android 15 上有更好的体验
 */
class SpamCallScreeningService : CallScreeningService() {

    companion object {
        private const val TAG = "SpamCallScreening"
        private const val CHANNEL_ID = "spam_block_channel"
        private const val NOTIFICATION_ID_BLOCKED = 1001
    }

    private val database by lazy { AppDatabase.getInstance(this) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onScreenCall(details: Call.Details) {
        val phoneNumber = PhoneNumberUtils.normalize(
            details.handle?.schemeSpecificPart ?: ""
        )

        Log.d(TAG, "Screening call from: $phoneNumber")

        if (phoneNumber.isBlank()) {
            // 无法识别号码，放行
            respondToCall(details, CallResponse.Builder().build())
            return
        }

        // 在 Room 中查询是否在黑名单中（同步方式，因为此回调需要在5秒内响应）
        val shouldBlock = runBlocking {
            val blocked = database.blockedNumberDao().findByPhoneNumber(phoneNumber)
            // 模糊匹配：也尝试用较短号码匹配
            if (blocked != null) {
                blocked.blockCalls
            } else {
                // 模糊匹配所有黑名单号码
                database.blockedNumberDao().getAllBlockedNumbers()
                    .first()
                    .any { bn ->
                        PhoneNumberUtils.matches(phoneNumber, bn.phoneNumber) && bn.blockCalls
                    }
            }
        }

        if (shouldBlock) {
            Log.i(TAG, "Blocking call from: $phoneNumber")
            // 拦截来电
            val response = CallResponse.Builder()
                .setRejectCall(true)
                .setSkipCallLog(false)
                .setSkipNotification(false)
                .build()
            respondToCall(details, response)

            // 显示拦截通知
            showBlockedCallNotification(phoneNumber)
        } else {
            // 放行
            respondToCall(details, CallResponse.Builder().build())
        }
    }

    /**
     * 显示来电被拦截的通知
     */
    private fun showBlockedCallNotification(phoneNumber: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val displayNumber = PhoneNumberUtils.formatForDisplay(phoneNumber)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setContentTitle("拦截骚扰来电")
            .setContentText("已拦截来自 $displayNumber 的来电")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(this).notify(
            NOTIFICATION_ID_BLOCKED,
            notification
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "骚扰拦截通知",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "显示被拦截的来电和短信通知"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}


