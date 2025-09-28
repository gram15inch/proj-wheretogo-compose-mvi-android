package com.wheretogo.data.datasource

import com.wheretogo.data.model.history.LocalHistoryGroupWrapper
import com.wheretogo.data.model.user.LocalProfile
import com.wheretogo.domain.HistoryType
import com.wheretogo.domain.model.user.History
import kotlinx.coroutines.flow.Flow

interface UserLocalDatasource {

    suspend fun addHistory(type: HistoryType, historyId: String, addedAt: Long): Result<Unit>

    suspend fun removeHistory(type: HistoryType, historyId: String): Result<Unit>

    suspend fun getHistory(type: HistoryType): LocalHistoryGroupWrapper

    suspend fun setProfile(profile: LocalProfile): Result<Unit>

    suspend fun setHistory(history: History): Result<Unit>

    suspend fun clearUser(): Result<Unit>

    fun getProfileFlow(): Flow<LocalProfile>
}