package com.wheretogo.data.datasource

interface AppRemoteDatasource {
    suspend fun getPublicToken(encryptedSignature: String): Result<String>

    suspend fun getPublicKey(apiAccessKey: String): Result<String>
}