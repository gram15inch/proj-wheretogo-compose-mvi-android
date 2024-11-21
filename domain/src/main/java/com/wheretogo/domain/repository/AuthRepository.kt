package com.wheretogo.domain.repository

interface AuthRepository {
    suspend fun signInWithGoogle(idToken: String)
    suspend fun signOutWithGoogle()
}