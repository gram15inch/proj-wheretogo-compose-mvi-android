package com.dhkim139.core.ui.screen

sealed class AppScreen {
    data object Home : AppScreen()
    data object Drive : AppScreen()
    data object CourseAdd : AppScreen()
    data object Gallery : AppScreen() {
        val route = "${Gallery}?openPicker={openPicker}"
        const val openPicker = "openPicker"
        fun args(openPicker: Boolean) = "${Gallery}?openPicker=$openPicker"
    }
    data object Checkin : AppScreen()
    data object Setting : AppScreen()
}