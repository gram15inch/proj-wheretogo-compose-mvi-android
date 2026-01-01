package com.wheretogo.data.repositoryimpl

import com.wheretogo.data.DataSettingAttr
import com.wheretogo.data.datasource.AppLocalDatasource
import com.wheretogo.data.datasource.AppRemoteDatasource
import com.wheretogo.data.feature.mapSuccess
import com.wheretogo.data.toDomainResult
import com.wheretogo.domain.DriveTutorialStep
import com.wheretogo.domain.model.app.Settings
import com.wheretogo.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AppRepositoryImpl @Inject constructor(
    private val appRemoteDatasource: AppRemoteDatasource,
    private val appLocalDatasource: AppLocalDatasource
) : AppRepository {

    override suspend fun observeSetting(): Flow<Settings> {
        return appLocalDatasource.observeInt(DataSettingAttr.TUTORIAL).map {
            Settings(tutorialStep = DriveTutorialStep.entries.getOrNull(it)?: DriveTutorialStep.SKIP)
        }
    }

    override suspend fun getSetting(): Result<Settings> {
        return appLocalDatasource.getInt(DataSettingAttr.TUTORIAL).map {
            Settings(tutorialStep = DriveTutorialStep.entries.getOrNull(it)?: DriveTutorialStep.SKIP)
        }.toDomainResult()
    }

    override suspend fun setTutorialStep(step: DriveTutorialStep): Result<Unit> {
        return appLocalDatasource.setInt(DataSettingAttr.TUTORIAL, step.ordinal)
            .toDomainResult()
    }

    override suspend fun refreshPublicToken(encryptedSignature: String): Result<Unit> {
        return appRemoteDatasource.getPublicToken(encryptedSignature)
            .mapSuccess { token ->
                appLocalDatasource.setPublicToken(token)
            }.map { }.toDomainResult()
    }

    override suspend fun getPublicToken(): Result<String> {
        return appLocalDatasource.getPublicToken().toDomainResult()
    }

    override suspend fun getPublicKey(): Result<String> {
        return appLocalDatasource.getApiAccessKey().mapSuccess { accessKey ->
            appRemoteDatasource.getPublicKey(accessKey).toDomainResult()
        }
    }
}