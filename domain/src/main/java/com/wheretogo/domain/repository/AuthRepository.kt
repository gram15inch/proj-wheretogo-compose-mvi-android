package com.wheretogo.domain.repository

import com.wheretogo.domain.model.auth.AuthToken
import com.wheretogo.domain.model.user.AuthProfile

interface AuthRepository {
    suspend fun signInWithToken(authToken: AuthToken): Result<AuthProfile>
    suspend fun signOut(): Result<Unit>
}