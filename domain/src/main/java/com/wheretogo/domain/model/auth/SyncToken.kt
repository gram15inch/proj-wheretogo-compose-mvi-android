package com.wheretogo.domain.model.auth

import com.wheretogo.domain.AuthCompany

data class SyncToken(
    val authCompany: AuthCompany = AuthCompany.PROFILE,
    val idToken: String,
    val msgToken: String
)