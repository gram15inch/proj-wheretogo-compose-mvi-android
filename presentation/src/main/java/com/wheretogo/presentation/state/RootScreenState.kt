package com.wheretogo.presentation.state

data class RootScreenState (
    val isSignInScreenVisible: Boolean = false,
    val error: String? = null
)