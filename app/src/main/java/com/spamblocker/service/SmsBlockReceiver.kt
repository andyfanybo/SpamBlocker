package com.spamblocker.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.spamblocker.MainActivity
import com.spamblocker.data.AppDatabase
import com.spamblocker.util.PhoneNumberUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * 短信拦截广播接收器
 * 监听 SMS_RECEIVED 广播，拦截黑名单号码发来的短信
 * 注意：abortBroadcast() 必须在 onReceive 返回前同步调用
 */
class SmsBlockReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "SmsBlockReceiver"
        private const val CHANNEL_ID = "spam_block_channel"
        private const val NOTIFICATION_ID_SMS_BLOCKED = 2001
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val database = AppDatabase.getInstance(context)

        for (message in messages) {
            val originatingAddress = PhoneNumberUtils.normalize(message.originatingAddress)

            // 同步查询（必须在 onReceive 内完成以支持 abortBroadcast）
            val shouldBlock = runBlocking {
                val exactMatch = database.blockedNumberDao().shouldBlockSms(originatingAddress)
                if (exactMatch == true) {
                    true
                } else {
                    // 模糊匹配所有黑名单号码
                    database.blockedNumberDao().getAllBlockedNumbers()
                        .first()
                        .any { bn ->
                            PhoneNumberUtils.matches(originatingAddress, bn.phoneNumber) && bn.blockSms
                        }
                }
            }

            if (shouldBlock) {
                abortBroadcast()
                showBlockedSmsNotification(
                    context,
                    originatingAddress,
                    message.messageBody ?: ""
                )
                Log.i(TAG, "Blocked SMS from: $originatingAddress")
            }
        }
    }

    private fun showBlockedSmsNotification(
        context: Context,
        phoneNumber: String,
        messageBody: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        createNotificationChannel(context)

        val displayNumber = PhoneNumberUtils.formatForDisplay(phoneNumber)
        val preview = if (messageBody.length > 50) {
            messageBody.take(50) + "..."
        } else {
            messageBody
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle("拦截骚扰短信")
            .setContentText("已拦截来自 $displayNumber 的短信")
            .setStyle(NotificationCompat.BigTextStyle().bigText("$displayNumber: $preview"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(
            NOTIFICATION_ID_SMS_BLOCKED,
            notification
        )
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "骚扰拦截通知",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "显示被拦截的来电和短信通知"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
