package com.wheretogo.data.datasource

import com.wheretogo.domain.HistoryType
import com.wheretogo.domain.model.map.History
import com.wheretogo.domain.model.user.Profile
import kotlinx.coroutines.flow.Flow

interface UserLocalDatasource {
    fun isRequestLoginFlow(): Flow<Boolean>

    suspend fun setRequestLogin(boolean: Boolean)

    suspend fun removeHistory(historyId: String, type: HistoryType)

    suspend fun addHistory(historyId: String, type: HistoryType)

    fun getHistoryFlow(type: HistoryType): Flow<HashSet<String>>

    suspend fun setProfile(profile: Profile)

    suspend fun setHistory(history: History)

    suspend fun clearUser()

    fun getProfileFlow(): Flow<Profile>
}