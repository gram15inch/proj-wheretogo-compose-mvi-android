package com.wheretogo.domain.model.user


data class AuthResponse(val isSuccess: Boolean, val data: AuthData? = null) {
    data class AuthData(val uid: String, val email: String, val userName: String)
}