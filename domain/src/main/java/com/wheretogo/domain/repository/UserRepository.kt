package com.wheretogo.domain.repository

import com.wheretogo.domain.HistoryType
import com.wheretogo.domain.model.user.AuthProfile
import com.wheretogo.domain.model.user.Profile
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getProfileStream(): Flow<Profile>

    suspend fun createUser(profile: Profile): Result<Unit>

    suspend fun syncUser(authProfile: AuthProfile): Result<Profile>

    suspend fun deleteUser(): Result<Unit>

    suspend fun checkUser(mail: String): Result<Profile>

    suspend fun getHistory(type: HistoryType): HashSet<String>

    suspend fun addHistory(
        historyId: String,
        type: HistoryType
    ): Result<Unit>


    suspend fun addHistoryToLocal(
        historyId: String,
        type: HistoryType
    ): Result<Unit>

    suspend fun removeHistory(
        historyId: String,
        type: HistoryType
    ): Result<Unit>

    suspend fun removeHistoryToLocal(
        historyId: String,
        type: HistoryType
    ): Result<Unit>

    suspend fun clearCache()
}