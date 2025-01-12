package com.wheretogo.presentation.intent

import com.wheretogo.presentation.InfoType

sealed class SettingIntent {
    data object UserDeleteClick : SettingIntent()
    data object LogoutClick : SettingIntent()
    data class InfoClick(val infoType: InfoType) : SettingIntent()
    data object UsernameChangeClick : SettingIntent()
}