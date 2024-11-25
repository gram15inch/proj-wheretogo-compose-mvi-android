package com.wheretogo.domain.repository

import com.wheretogo.domain.model.user.AuthResponse

interface AuthRepository {
    suspend fun authWithGoogle(idToken: String): AuthResponse
    suspend fun signOutOnFirebase()
}