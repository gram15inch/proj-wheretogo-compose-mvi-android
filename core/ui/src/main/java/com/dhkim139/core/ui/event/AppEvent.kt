package com.dhkim139.core.ui.event

import com.dhkim139.core.ui.screen.AppScreen
import com.dhkim139.core.ui.model.EventMsg
import com.dhkim139.core.ui.permission.AppPermission

sealed class AppEvent {
    data class Navigation(val from: AppScreen?, val to: AppScreen, val inclusive: Boolean = true) : AppEvent()
    data class SnackBar(val msg: EventMsg) : AppEvent()
    data class Permission(val permission: AppPermission) : AppEvent()
    data object SignInScreen : AppEvent()
}