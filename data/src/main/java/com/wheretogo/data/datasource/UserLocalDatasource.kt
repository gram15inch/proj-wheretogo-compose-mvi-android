package com.wheretogo.data.datasource

import com.wheretogo.data.model.user.LocalProfile
import com.wheretogo.domain.HistoryType
import com.wheretogo.domain.model.map.History
import kotlinx.coroutines.flow.Flow

interface UserLocalDatasource {
    fun isRequestLoginFlow(): Flow<Boolean>

    suspend fun setRequestLogin(boolean: Boolean)

    suspend fun removeHistory(historyId: String, type: HistoryType)

    suspend fun addHistory(historyId: String, type: HistoryType)

    suspend fun setHistoryGroup(historyIdGroup: HashSet<String>, type: HistoryType)

    fun getHistoryFlow(type: HistoryType): Flow<HashSet<String>>

    fun getTokenFlow(): Flow<String>

    suspend fun setProfile(profile: LocalProfile)

    suspend fun setHistory(history: History)

    suspend fun setToken(token:String)

    suspend fun clearUser()

    fun getProfileFlow(): Flow<LocalProfile>
}