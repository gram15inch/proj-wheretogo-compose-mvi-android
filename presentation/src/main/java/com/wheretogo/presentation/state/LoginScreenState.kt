package com.wheretogo.presentation.state

data class LoginScreenState(
    val isExit: Boolean = false,
    val isLoading: Boolean = false,
    val toastMsg: String = "",
    val error: String? = null
)