package com.wheretogo.presentation.state

data class RootScreenState (
    val isRequestLogin: Boolean = false,
    val error: String? = null
)