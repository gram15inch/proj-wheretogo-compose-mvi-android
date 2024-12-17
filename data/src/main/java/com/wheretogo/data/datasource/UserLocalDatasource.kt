package com.wheretogo.data.datasource

import com.wheretogo.domain.HistoryType
import com.wheretogo.domain.model.user.Profile
import kotlinx.coroutines.flow.Flow

interface UserLocalDatasource {
    fun isRequestLoginFlow(): Flow<Boolean>

    suspend fun setRequestLogin(boolean: Boolean)

    suspend fun removeHistory(code: String, type: HistoryType)

    suspend fun addHistory(code: String, type: HistoryType)

    fun getHistoryFlow(type: HistoryType): Flow<List<String>>

    suspend fun setProfile(profile: Profile)

    suspend fun clearUser()

    suspend fun clearHistory()

    fun getProfileFlow(): Flow<Profile>
}