package com.spamblocker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telecom.TelecomManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.spamblocker.ui.MainViewModel
import com.spamblocker.ui.screens.AddNumberScreen
import com.spamblocker.ui.screens.HomeScreen
import com.spamblocker.ui.theme.SpamBlockerTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by lazy { MainViewModel(application) }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* 权限结果处理 */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 检查并请求必要权限
        checkAndRequestPermissions()

        // 检查 CallScreening 角色
        checkCallScreeningRole()

        setContent {
            SpamBlockerTheme {
                var showAddScreen by remember { mutableStateOf(false) }

                if (showAddScreen) {
                    AddNumberScreen(
                        viewModel = viewModel,
                        onBack = { showAddScreen = false }
                    )
                } else {
                    HomeScreen(
                        viewModel = viewModel,
                        onAddClick = { showAddScreen = true }
                    )
                }
            }
        }
    }

    /**
     * 检查必要权限
     */
    private fun checkAndRequestPermissions() {
        // Android 13+ 需要通知权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // 电话状态权限
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.READ_PHONE_STATE),
                REQUEST_PHONE_PERMISSION
            )
        }
    }

    /**
     * 检查来电筛查服务角色
     */
    private fun checkCallScreeningRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(android.app.role.RoleManager::class.java)
            if (roleManager != null && !roleManager.isRoleHeld(android.app.role.RoleManager.ROLE_CALL_SCREENING)) {
                val intent = roleManager.createRequestRoleIntent(android.app.role.RoleManager.ROLE_CALL_SCREENING)
                startActivityForResult(intent, REQUEST_CALL_SCREENING_ROLE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CALL_SCREENING_ROLE -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val roleManager = getSystemService(android.app.role.RoleManager::class.java)
                    if (roleManager?.isRoleHeld(android.app.role.RoleManager.ROLE_CALL_SCREENING) == true) {
                        Toast.makeText(this, "来电拦截服务已启用 ✓", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "需要授权来电拦截权限才能拦截骚扰电话", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    companion object {
        private const val REQUEST_PHONE_PERMISSION = 101
        private const val REQUEST_CALL_SCREENING_ROLE = 102
    }
}
