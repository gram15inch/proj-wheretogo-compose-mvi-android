package com.wheretogo.domain.repository

import com.wheretogo.domain.model.user.AuthResponse

interface AuthRepository {
    suspend fun googleSignIn(): AuthResponse
    suspend fun signOut()
    suspend fun deleteUser(): Boolean
}