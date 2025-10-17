package com.wheretogo.domain.repository

interface AppRepository {
    suspend fun refreshPublicToken(encryptedSignature: String): Result<Unit>
    suspend fun getPublicToken(): Result<String>
    suspend fun getPublicKey(): Result<String>
}