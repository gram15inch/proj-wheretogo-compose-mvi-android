package com.wheretogo.data.datasource

interface AppRemoteDatasource {
    suspend fun getPublicKey(): Result<String>
    suspend fun getPublicToken(encryptedSignature: String): Result<String>
}