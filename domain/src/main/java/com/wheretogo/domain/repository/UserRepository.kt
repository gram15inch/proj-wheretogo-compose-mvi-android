package com.wheretogo.domain.repository

import com.wheretogo.domain.HistoryType
import com.wheretogo.domain.model.user.AuthProfile
import com.wheretogo.domain.model.user.Profile
import com.wheretogo.domain.model.user.ProfilePrivate
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun isRequestLoginStream(): Flow<Boolean>

    suspend fun setRequestLogin(boolean: Boolean)

    suspend fun addHistory(
        userId: String = "",
        historyId: String,
        type: HistoryType
    ): Result<Unit>

    suspend fun setHistoryGroup(
        userId: String = "",
        historyGroup: HashSet<String>,
        type: HistoryType
    ): Result<Unit>

    suspend fun removeHistory(
        userId: String,
        historyId: String,
        type: HistoryType
    ): Result<Unit>

    suspend fun removeHistoryGroup(
        userId: String,
        historyIdGroup: HashSet<String>,
        type: HistoryType
    ): Result<Unit>

    suspend fun getHistoryIdStream(type: HistoryType): Flow<HashSet<String>>

    suspend fun getProfileStream(): Flow<Profile>

    suspend fun getProfile(userId: String): Result<Profile>

    suspend fun getProfilePrivate(userId: String): Result<ProfilePrivate>

    suspend fun setProfile(profile: Profile): Result<Unit>

    suspend fun createUser(profile: Profile): Result<Unit>

    suspend fun syncUser(authProfile: AuthProfile): Result<Profile>

    suspend fun clearUserCache()

    suspend fun deleteUser(): Result<Unit>

    suspend fun checkUser(mail:String): Result<Profile>
}