package com.wheretogo.presentation.intent

import com.wheretogo.presentation.AppLifecycle
import com.wheretogo.presentation.SettingInfoType

sealed class SettingIntent {
    data object UserDeleteClick : SettingIntent()
    data object LogoutClick : SettingIntent()
    data class InfoClick(val settingInfoType: SettingInfoType) : SettingIntent()
    data object UsernameChangeClick : SettingIntent()
    data class DialogAnswer(val answer: Boolean) : SettingIntent()
    data class LifecycleChange(val event: AppLifecycle) : SettingIntent()
}