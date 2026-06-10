package com.wheretogo.presentation.feature

import android.Manifest
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
    val check = checkFalseOrData(context, permission)
    val isRetry = when (check) {
        MediaAccess.PARTIAL -> true
        false -> true
        else -> false
    }

    var isDenied = check == false

    if(isRetry){
        isDenied = withContext(Dispatchers.IO) {
            !EventBus.sendWithResult(AppEvent.Permission(permission))
        }
    }

    if (isDenied) {
        val isNeedGuide = !ActivityCompat.shouldShowRequestPermissionRationale(
            context as Activity,
            permission.names.firstOrNull()?:return false
        )

        if (isNeedGuide) {
            openSetting(context,permission)
        }
        return false
    }
    return true
}

suspend fun openSetting(context: Context, permission: AppPermission){
    val strRes= when(permission){
        AppPermission.LOCATION -> R.string.grant_location_permission
        AppPermission.MEDIA -> R.string.grant_picture_permission
        else -> R.string.grant_permission
    }

    val intentUri = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }.toUri(Intent.URI_INTENT_SCHEME)

    EventBus.send(
        AppEvent.SnackBar(
            EventMsg(
                strRes = strRes,
                labelRes = R.string.setting_open,
                uri = intentUri
            )
        )
    )
}

enum class MediaAccess { FULL, PARTIAL }

fun checkFalseOrData(context: Context, appPermission: AppPermission): Any {
    if(appPermission.names.isEmpty())
        return true
    val granted = { p: String ->
        ContextCompat.checkSelfPermission(context, p) == PackageManager.PERMISSION_GRANTED
    }
    return when (appPermission) {
        AppPermission.MEDIA -> {
            when {
                granted(Manifest.permission.READ_MEDIA_IMAGES) -> MediaAccess.FULL
                granted(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) -> MediaAccess.PARTIAL
                granted(Manifest.permission.READ_EXTERNAL_STORAGE) -> MediaAccess.FULL
                else -> false
            }
        }

        else -> {
            appPermission.names.forEach {
                if(!granted(it)) return false
            }
            true
        }
    }
}