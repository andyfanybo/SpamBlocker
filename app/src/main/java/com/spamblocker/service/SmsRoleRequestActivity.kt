package com.spamblocker.service

import android.app.Activity
import android.os.Bundle
import android.provider.Telephony
import android.widget.Toast

/**
 * 引导用户将本应用设为默认短信应用以获得完整拦截能力
 */
class SmsRoleRequestActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // Android 10+ 使用 RoleManager 请求默认短信角色
            val roleManager = getSystemService(android.app.role.RoleManager::class.java)
            if (roleManager != null && !roleManager.isRoleHeld(android.app.role.RoleManager.ROLE_SMS)) {
                val intent = roleManager.createRequestRoleIntent(android.app.role.RoleManager.ROLE_SMS)
                startActivityForResult(intent, REQUEST_SMS_ROLE)
                return
            }
        }

        Toast.makeText(this, "本应用已是默认短信应用", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SMS_ROLE) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                val roleManager = getSystemService(android.app.role.RoleManager::class.java)
                if (roleManager?.isRoleHeld(android.app.role.RoleManager.ROLE_SMS) == true) {
                    Toast.makeText(this, "已设为默认短信应用，骚扰短信将被拦截", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "未设置成功，短信拦截功能受限", Toast.LENGTH_SHORT).show()
                }
            }
        }
        finish()
    }

    companion object {
        private const val REQUEST_SMS_ROLE = 100
    }
}
