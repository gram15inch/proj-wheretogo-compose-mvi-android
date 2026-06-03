package com.wheretogo.presentation.feature

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.wheretogo.presentation.AppEvent
import com.wheretogo.presentation.AppPermission
import com.wheretogo.presentation.R
import com.wheretogo.presentation.model.EventMsg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun requestPermission(context: Context, permission: AppPermission): Boolean {
    val checkPermission = ContextCompat.checkSelfPermission(context, permission.name)
    val isDenied = checkPermission == PackageManager.PERMISSION_DENIED

    if (isDenied) {
        val isRejected = withContext(Dispatchers.IO) {
            !EventBus.sendWithResult(AppEvent.Permission(permission))
        }

        if (isRejected) {
            val isNeedGuide = !ActivityCompat.shouldShowRequestPermissionRationale(
                context as Activity,
                permission.name
            )

            if (isNeedGuide) {
                val intentUri = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }.toUri(Intent.URI_INTENT_SCHEME)

                EventBus.send(
                    AppEvent.SnackBar(
                        EventMsg(
                            strRes = R.string.grant_location_permission,
                            labelRes = R.string.setting_open,
                            uri = intentUri
                        )
                    )
                )
            }
            return false
        }
    }
    return true
}