package com.wheretogo.domain.model.auth

import com.wheretogo.domain.AuthType
import com.wheretogo.domain.model.user.AuthProfile

data class AuthRequest(
    val authType: AuthType,
    val authToken: AuthToken? = null,
    val authProfile: AuthProfile? = null
)