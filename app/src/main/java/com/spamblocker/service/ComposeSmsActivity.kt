package com.spamblocker.service

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Telephony
import android.widget.Toast

/**
 * 快捷短信发送 Activity
 * 默认短信应用需要处理 SEND / SENDTO intent
 * 此处仅做占位，将请求转发给系统的默认短信应用
 */
class ComposeSmsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 将发送短信的请求转发给用户常用的短信应用
        val forwardIntent = Intent(Intent.ACTION_VIEW).apply {
            data = intent.data
            putExtra("address", intent.getStringExtra("address"))
            putExtra("sms_body", intent.getStringExtra("sms_body"))
            // 使用系统选择器让用户选择短信应用
            val chooser = Intent.createChooser(
                Intent(Intent.ACTION_SENDTO, intent.data).apply {
                    putExtra("sms_body", intent.getStringExtra("sms_body"))
                },
                "发送短信"
            )
            startActivity(chooser)
        }

        Toast.makeText(this, "请选择短信应用发送", Toast.LENGTH_SHORT).show()
        finish()
    }
}
