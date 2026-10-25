package com.wheretogo.data.repositoryimpl

import com.wheretogo.data.datasource.SettingsLocalDataSource
import com.wheretogo.domain.repository.SettingsRepository
import javax.inject.Inject


class SettingsRepositoryImpl @Inject constructor(
    private val local: SettingsLocalDataSource,
) : SettingsRepository {

    override suspend fun mapAppId(): String? = local.mapAppId()

    override suspend fun saveMapAppId(id: String) {
        local.setMapAppId(id)
    }
}
