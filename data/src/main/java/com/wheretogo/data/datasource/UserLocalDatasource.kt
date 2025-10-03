package com.wheretogo.data.datasource

import com.wheretogo.data.model.history.LocalHistory
import com.wheretogo.data.model.history.LocalHistoryGroupWrapper
import com.wheretogo.data.model.user.LocalProfile
import com.wheretogo.domain.HistoryType
import kotlinx.coroutines.flow.Flow

interface UserLocalDatasource {

    suspend fun addHistory(type: HistoryType, groupId: String, historyId: String, addedAt: Long): Result<Unit>

    suspend fun removeHistory(type: HistoryType, groupId: String, historyId: String): Result<Unit>

    suspend fun getHistory(type: HistoryType): LocalHistoryGroupWrapper

    suspend fun setProfile(profile: LocalProfile): Result<Unit>

    suspend fun iniHistory(history: LocalHistory): Result<Unit>

    suspend fun clearUser(): Result<Unit>

    fun getProfileFlow(): Flow<LocalProfile>
}