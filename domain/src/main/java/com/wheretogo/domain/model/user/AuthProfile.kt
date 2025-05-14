package com.wheretogo.domain.model.user

import com.wheretogo.domain.AuthCompany

data class AuthProfile(
    val uid: String,
    val email: String,
    val userName: String,
    val authCompany: AuthCompany = AuthCompany.PROFILE,
    val token: String
)