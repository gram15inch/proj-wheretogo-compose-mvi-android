package com.wheretogo.domain.model.auth

import com.wheretogo.domain.AuthCompany

data class SyncProfile(
    val uid: String,
    val mail: String,
    val name: String,
    val authCompany: AuthCompany = AuthCompany.PROFILE,
    val idToken: String,
    val msgToken: String
)
