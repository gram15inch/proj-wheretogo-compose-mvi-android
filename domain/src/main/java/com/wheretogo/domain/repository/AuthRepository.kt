package com.wheretogo.domain.repository

import com.wheretogo.domain.model.auth.SignToken
import com.wheretogo.domain.model.auth.SyncProfile

interface AuthRepository {
    suspend fun signInWithToken(signToken: SignToken): Result<SyncProfile>
    suspend fun signOut(): Result<Unit>
}