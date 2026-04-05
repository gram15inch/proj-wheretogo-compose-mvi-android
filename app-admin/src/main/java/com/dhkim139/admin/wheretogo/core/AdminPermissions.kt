package com.dhkim139.admin.wheretogo.core

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

class NotificationPermissionHelper(
    private val activity: ComponentActivity
) {

    private val launcher = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            showPermissionDeniedDialog()
        }
    }

    fun requestIfNeeded() {
        when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU -> return

            hasPermission() -> return

            else -> launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(activity)
            .setTitle("알림 권한 필요")
            .setMessage("알림을 받으려면 권한이 필요합니다.\n설정에서 허용해주세요.")
            .setPositiveButton("설정으로 이동") { _, _ -> openAppSettings() }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", activity.packageName, null)
        }
        activity.startActivity(intent)
    }
}