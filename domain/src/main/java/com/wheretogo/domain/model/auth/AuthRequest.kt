package com.wheretogo.domain.model.auth

import com.wheretogo.domain.AuthType

data class AuthRequest(
    val authType: AuthType,
    val signToken: SignToken? = null,
    val signProfile: SignProfile? = null
)