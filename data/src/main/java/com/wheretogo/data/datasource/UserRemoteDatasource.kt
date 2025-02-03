package com.wheretogo.data.datasource

import com.wheretogo.data.model.history.RemoteHistoryGroupWrapper
import com.wheretogo.data.model.user.ProfilePublic
import com.wheretogo.domain.HistoryType
import com.wheretogo.domain.model.user.ProfilePrivate

interface UserRemoteDatasource {
    suspend fun setProfilePublic(public: ProfilePublic): Boolean
    suspend fun setProfilePrivate(uid: String, privateProfile: ProfilePrivate): Boolean
    suspend fun getProfilePublic(uid: String): ProfilePublic?
    suspend fun getProfilePublicWithMail(hashMail: String): ProfilePublic?
    suspend fun getProfilePrivate(uid: String): ProfilePrivate?
    suspend fun deleteProfile(uid: String): Boolean

    suspend fun addHistory(uid: String, historyId: String, type: HistoryType): Boolean
    suspend fun getHistoryGroup(uid: String, type: HistoryType): Pair<HistoryType, HashSet<String>>
    suspend fun setHistoryGroup(uid: String, wrapper: RemoteHistoryGroupWrapper): Boolean

}