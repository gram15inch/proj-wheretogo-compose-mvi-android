package com.wheretogo.domain.repository

import com.wheretogo.domain.HistoryType
import com.wheretogo.domain.model.user.Profile
import com.wheretogo.domain.model.user.SignResponse
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun isRequestLoginStream(): Flow<Boolean>

    suspend fun setRequestLogin(boolean: Boolean)

    suspend fun addHistory(historyId: String, type: HistoryType)

    suspend fun setHistoryGroup(uId: String, historyGroup: HashSet<String>, type: HistoryType)

    suspend fun removeHistory(id: String, type: HistoryType)

    suspend fun getHistoryIdStream(type: HistoryType): Flow<HashSet<String>>

    suspend fun getProfileStream(): Flow<Profile>

    suspend fun getProfile(uid: String): Profile?

    suspend fun setProfile(profile: Profile): Boolean

    suspend fun signUp(profile: Profile): SignResponse

    suspend fun signIn(uid: String): SignResponse

    suspend fun signOut(): SignResponse
}