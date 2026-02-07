package com.wheretogo.data.datasource

import com.wheretogo.data.DataSettingAttr
import kotlinx.coroutines.flow.Flow

interface AppLocalDatasource {
    suspend fun observeInt(key: DataSettingAttr): Flow<Int>
    suspend fun getInt(key: DataSettingAttr): Result<Int>
    suspend fun setInt(key: DataSettingAttr, num: Int): Result<Unit>

    suspend fun getPublicToken(): Result<String>
    suspend fun setPublicToken(token: String): Result<Unit>
}