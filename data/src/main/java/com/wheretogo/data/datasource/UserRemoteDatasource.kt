package com.wheretogo.data.datasource

import com.wheretogo.data.model.history.RemoteHistoryGroupWrapper
import com.wheretogo.domain.HistoryType
import com.wheretogo.domain.model.user.Profile

interface UserRemoteDatasource {
    suspend fun setProfile(profile: Profile): Boolean
    suspend fun getProfile(uid: String): Profile?

    suspend fun addHistory(uid: String, historyId: String, type: HistoryType): Boolean
    suspend fun getHistoryGroup(uid: String, type: HistoryType): HashSet<String>
    suspend fun setHistoryGroup(uid: String, wrapper: RemoteHistoryGroupWrapper): Boolean
}