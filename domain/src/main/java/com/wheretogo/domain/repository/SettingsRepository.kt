package com.wheretogo.domain.repository

interface SettingsRepository {

    suspend fun mapAppId(): String?

    suspend fun saveMapAppId(id: String)
}