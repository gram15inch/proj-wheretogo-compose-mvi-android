package com.wheretogo.presentation.state

data class MainScreenState (
    val isRequestLogin: Boolean = false,
    val error: String? = null
)