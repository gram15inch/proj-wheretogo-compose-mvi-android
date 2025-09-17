package com.wheretogo.data.datasource

import com.wheretogo.data.model.user.LocalProfile
import com.wheretogo.domain.HistoryType
import com.wheretogo.domain.model.user.History
import kotlinx.coroutines.flow.Flow

interface UserLocalDatasource {

    suspend fun removeHistory(historyId: String, type: HistoryType): Result<Unit>

    suspend fun addHistory(historyId: String, type: HistoryType): Result<Unit>

    suspend fun setHistoryGroup(historyIdGroup: HashSet<String>, type: HistoryType): Result<Unit>

    fun getHistoryFlow(type: HistoryType): Flow<HashSet<String>>

    suspend fun setProfile(profile: LocalProfile): Result<Unit>

    suspend fun setHistory(history: History): Result<Unit>

    suspend fun clearUser(): Result<Unit>

    fun getProfileFlow(): Flow<LocalProfile>
}