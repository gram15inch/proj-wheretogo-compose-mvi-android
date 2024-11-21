package com.wheretogo.presentation.state

data class LoginScreenState(
    val isExit: Boolean = false,
    val error: String? = null
)