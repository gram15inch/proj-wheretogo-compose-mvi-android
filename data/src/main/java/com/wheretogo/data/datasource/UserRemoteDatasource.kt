package com.wheretogo.data.datasource

import com.wheretogo.data.model.history.RemoteHistoryGroupWrapper
import com.wheretogo.data.model.user.RemoteProfilePrivate
import com.wheretogo.data.model.user.RemoteProfilePublic
import com.wheretogo.domain.HistoryType

interface UserRemoteDatasource {
    suspend fun setProfilePublic(public: RemoteProfilePublic)
    suspend fun setProfilePrivate(uid: String, privateProfile: RemoteProfilePrivate)
    suspend fun getProfilePublic(uid: String): RemoteProfilePublic?
    suspend fun getProfilePublicWithMail(hashMail: String): RemoteProfilePublic?
    suspend fun getProfilePrivate(uid: String): RemoteProfilePrivate?
    suspend fun deleteProfile(uid: String)
    suspend fun deleteUser(userId: String): Result<String>

    suspend fun addHistory(uid: String, historyId: String, type: HistoryType)
    suspend fun getHistoryGroup(uid: String, type: HistoryType): Pair<HistoryType, HashSet<String>>
    suspend fun setHistoryGroup(uid: String, wrapper: RemoteHistoryGroupWrapper)
    suspend fun deleteHistory(uid: String,type:HistoryType)
}