package com.wheretogo.data.datasource

interface SettingsLocalDataSource {

    suspend fun mapAppId(): String?

    suspend fun setMapAppId(id: String)
}