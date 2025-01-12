package com.wheretogo.domain.repository

import com.wheretogo.domain.HistoryType
import com.wheretogo.domain.model.user.Profile
import com.wheretogo.domain.model.user.ProfilePrivate
import com.wheretogo.domain.model.user.ProfilePublic
import com.wheretogo.domain.model.user.SignResponse
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun isRequestLoginStream(): Flow<Boolean>

    suspend fun setRequestLogin(boolean: Boolean)

    suspend fun addHistory(
        userId: String = "",
        historyId: String,
        type: HistoryType
    )

    suspend fun setHistoryGroup(
        userId: String = "",
        historyGroup: HashSet<String>,
        type: HistoryType
    )

    suspend fun removeHistory(
        userId: String,
        historyId: String,
        type: HistoryType
    )

    suspend fun getHistoryIdStream(type: HistoryType): Flow<HashSet<String>>

    suspend fun getProfileStream(): Flow<Profile>

    suspend fun getPublicProfile(userId: String): ProfilePublic?

    suspend fun getProfilePrivate(userId: String): ProfilePrivate?

    suspend fun setProfile(profile: Profile): Boolean

    suspend fun signUp(profile: Profile): SignResponse

    suspend fun signIn(uid: String): SignResponse

    suspend fun signOut(): SignResponse

    suspend fun deleteUser(userId: String): Boolean
}