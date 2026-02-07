package com.wheretogo.data.datasourceimpl

import com.wheretogo.data.DataError
import com.wheretogo.data.datasource.AppRemoteDatasource
import com.wheretogo.data.datasourceimpl.service.AppApiService
import com.wheretogo.data.feature.dataErrorCatching
import com.wheretogo.data.feature.mapSuccess
import com.wheretogo.data.toDataError
import com.wheretogo.domain.model.app.AppBuildConfig
import javax.inject.Inject

class AppRemoteDatasourceImpl @Inject constructor(
    private val appApiService: AppApiService,
    private val appBuildConfig: AppBuildConfig
) : AppRemoteDatasource {

    override suspend fun getPublicKey(): Result<String> {
        return dataErrorCatching { appApiService.getPublicKey(appBuildConfig.tokenRequestKey) }.mapSuccess {
            when {
                !it.isSuccessful -> Result.failure(it.toDataError())
                it.body()?.message.isNullOrBlank() -> Result.failure(DataError.NotFound("empty message"))
                else -> Result.success(it.body()?.message ?: "")
            }
        }
    }

    override suspend fun getPublicToken(encryptedSignature: String): Result<String> {
        return dataErrorCatching { appApiService.getPublicToken(encryptedSignature) }.mapSuccess {
            when {
                !it.isSuccessful -> Result.failure(it.toDataError())
                it.body()?.message.isNullOrBlank() -> Result.failure(DataError.NotFound("empty message"))
                else -> Result.success(it.body()?.message ?: "")
            }
        }
    }

}