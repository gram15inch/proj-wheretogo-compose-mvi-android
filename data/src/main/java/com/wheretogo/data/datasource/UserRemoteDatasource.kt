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

    suspend fun getHistoryGroup(uid: String): Result<List<RemoteHistoryGroupWrapper>>
    suspend fun setHistoryGroup(uid: String, wrapper: RemoteHistoryGroupWrapper): Result<Long>
    suspend fun addHistory(type: HistoryType, uid: String, groupId: String, historyId: String): Result<Long>
    suspend fun removeHistory(type: HistoryType, uid: String, groupId: String, historyId: String): Result<Unit>
}