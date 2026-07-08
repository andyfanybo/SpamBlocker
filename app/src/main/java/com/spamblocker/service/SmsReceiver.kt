package com.spamblocker.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.spamblocker.data.AppDatabase
import com.spamblocker.util.PhoneNumberUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * 短信接收器 (作为默认短信应用时使用)
 * 监听 SMS_DELIVER，拦截黑名单号码的短信，放行其他短信
 */
class SmsReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "SmsReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != android.provider.Telephony.Sms.Intents.SMS_DELIVER_ACTION) return

        val messages = android.provider.Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val database = AppDatabase.getInstance(context)

        for (message in messages) {
            val originatingAddress = PhoneNumberUtils.normalize(message.originatingAddress)
            Log.d(TAG, "SMS received from: $originatingAddress")

            val shouldBlock = runBlocking {
                val exactMatch = database.blockedNumberDao().shouldBlockSms(originatingAddress)
                if (exactMatch == true) {
                    true
                } else {
                    database.blockedNumberDao().getAllBlockedNumbers()
                        .first()
                        .any { bn ->
                            PhoneNumberUtils.matches(originatingAddress, bn.phoneNumber) && bn.blockSms
                        }
                }
            }

            if (shouldBlock) {
                abortBroadcast()
                Log.i(TAG, "Blocked SMS from: $originatingAddress")
            }
        }
    }
}
