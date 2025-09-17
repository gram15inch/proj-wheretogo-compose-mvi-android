package com.wheretogo.data.datasource

import com.wheretogo.data.model.history.RemoteHistoryGroupWrapper
import com.wheretogo.data.model.user.RemoteProfilePrivate
import com.wheretogo.data.model.user.RemoteProfilePublic
import com.wheretogo.domain.HistoryType

interface UserRemoteDatasource {
    suspend fun setProfilePublic(public: RemoteProfilePublic): Result<Unit>
    suspend fun setProfilePrivate(uid: String, privateProfile: RemoteProfilePrivate): Result<Unit>
    suspend fun getProfilePublic(uid: String): Result<RemoteProfilePublic>
    suspend fun getProfilePublicWithMail(hashMail: String): Result<RemoteProfilePublic>
    suspend fun getProfilePrivate(uid: String): Result<RemoteProfilePrivate>
    suspend fun deleteProfile(uid: String): Result<Unit>
    suspend fun deleteUser(userId: String): Result<String>

    suspend fun addHistory(uid: String, historyId: String, type: HistoryType): Result<Unit>
    suspend fun getHistoryGroup(uid: String): Result<Map<HistoryType, HashSet<String>>>
    suspend fun setHistoryGroup(uid: String, wrapper: RemoteHistoryGroupWrapper): Result<Unit>
    suspend fun deleteHistory(uid: String, type: HistoryType): Result<Unit>
}