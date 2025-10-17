package com.wheretogo.data.datasource

interface AppLocalDatasource {
    suspend fun getApiAccessKey(): Result<String>
    suspend fun getPublicToken(): Result<String>
    suspend fun setPublicToken(token: String): Result<Unit>
}