package com.wheretogo.data.datasource

import com.wheretogo.data.DataHistoryType
import com.wheretogo.data.model.history.LocalHistory
import com.wheretogo.data.model.history.LocalHistoryGroupWrapper
import com.wheretogo.data.model.user.LocalProfile
import kotlinx.coroutines.flow.Flow

interface UserLocalDatasource {

    suspend fun addHistory(type: DataHistoryType, groupId: String, historyId: String, addedAt: Long): Result<Unit>

    suspend fun removeHistory(type: DataHistoryType, groupId: String, historyId: String): Result<Unit>

    suspend fun getHistory(type: DataHistoryType): LocalHistoryGroupWrapper

    suspend fun setProfile(profile: LocalProfile): Result<Unit>

    suspend fun iniHistory(history: LocalHistory): Result<Unit>

    suspend fun clearUser(): Result<Unit>

    fun getProfileFlow(): Flow<LocalProfile>
}