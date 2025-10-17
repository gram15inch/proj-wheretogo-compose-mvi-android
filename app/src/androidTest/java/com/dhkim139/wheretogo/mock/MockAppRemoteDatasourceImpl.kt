package com.dhkim139.wheretogo.mock

import com.wheretogo.data.datasource.AppRemoteDatasource
import javax.inject.Inject

class MockAppRemoteDatasourceImpl @Inject constructor() : AppRemoteDatasource {
    override suspend fun getPublicToken(encryptedSignature: String): Result<String> {
        return Result.success("")
    }

    override suspend fun getPublicKey(apiAccessKey: String): Result<String> {
        return Result.success("")
    }
}