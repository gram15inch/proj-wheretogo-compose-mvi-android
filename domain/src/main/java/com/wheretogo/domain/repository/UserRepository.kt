package com.wheretogo.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun isRequestLoginFlow(): Flow<Boolean>

    suspend fun setRequestLogin(boolean: Boolean)
}