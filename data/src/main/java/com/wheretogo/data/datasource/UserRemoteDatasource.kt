package com.wheretogo.data.datasource

import com.wheretogo.data.model.history.RemoteHistoryGroupWrapper
import com.wheretogo.domain.HistoryType
import com.wheretogo.domain.model.user.ProfilePrivate
import com.wheretogo.domain.model.user.ProfilePublic

interface UserRemoteDatasource {
    suspend fun setProfilePublic(uid: String, profile: ProfilePublic): Boolean
    suspend fun setProfilePrivate(uid: String, profile: ProfilePrivate): Boolean
    suspend fun getProfilePublic(uid: String): ProfilePublic?
    suspend fun getProfilePrivate(uid: String): ProfilePrivate?

    suspend fun addHistory(uid: String, historyId: String, type: HistoryType): Boolean
    suspend fun getHistoryGroup(uid: String, type: HistoryType): HashSet<String>
    suspend fun setHistoryGroup(uid: String, wrapper: RemoteHistoryGroupWrapper): Boolean
}