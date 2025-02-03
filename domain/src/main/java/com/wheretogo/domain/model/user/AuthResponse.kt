package com.wheretogo.domain.model.user


data class AuthResponse(val isSuccess: Boolean, val data: AuthProfile? = null)