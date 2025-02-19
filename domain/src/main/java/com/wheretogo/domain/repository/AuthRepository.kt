package com.wheretogo.domain.repository

import com.wheretogo.domain.model.auth.AuthToken
import com.wheretogo.domain.model.user.AuthResponse

interface AuthRepository {
    suspend fun signInWithToken(authToken: AuthToken): AuthResponse
    suspend fun signOut()
    suspend fun deleteUser(): Boolean
}