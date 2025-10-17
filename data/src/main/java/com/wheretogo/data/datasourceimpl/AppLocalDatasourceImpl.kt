package com.wheretogo.data.datasourceimpl

import com.wheretogo.data.DataError
import com.wheretogo.data.datasource.AppLocalDatasource
import com.wheretogo.data.datasourceimpl.store.SecureStore
import com.wheretogo.data.feature.dataErrorCatching
import com.wheretogo.data.model.key.AppKey
import javax.inject.Inject

class AppLocalDatasourceImpl @Inject constructor(
    private val secureStore: SecureStore,
    private val key: AppKey
) : AppLocalDatasource {

    override suspend fun getApiAccessKey(): Result<String> {
        return Result.success(key.apiAccessKey)
    }

    override suspend fun getPublicToken(): Result<String> {
        return dataErrorCatching {
            val token = secureStore.getPublicToken()
            token ?: throw DataError.NotFound("token load fail")
        }
    }

    override suspend fun setPublicToken(token: String): Result<Unit> {
        return dataErrorCatching {
            secureStore.setPublicToken(token)
        }
    }

}