package com.wheretogo.domain.repository

import com.wheretogo.domain.HistoryType
import com.wheretogo.domain.model.history.HistoryGroupWrapper
import com.wheretogo.domain.model.user.AuthProfile
import com.wheretogo.domain.model.user.Profile
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getProfileStream(): Flow<Profile>

    suspend fun createUser(profile: Profile): Result<Unit>

    suspend fun syncUser(authProfile: AuthProfile): Result<Profile>

    suspend fun deleteUser(): Result<Unit>

    suspend fun checkUser(mail: String): Result<Profile>

    suspend fun getHistory(type: HistoryType): Result<HistoryGroupWrapper>

    suspend fun addHistory(
        type: HistoryType,
        groupId: String,
        historyId: String
    ): Result<Unit>

    suspend fun addHistoryToLocal(
        type: HistoryType,
        groupId: String,
        historyId: String,
        addedAt: Long = 0
    ): Result<Unit>

    suspend fun removeHistory(
        type: HistoryType,
        groupId: String,
        historyId: String
    ): Result<Unit>

    suspend fun removeHistoryToLocal(
        type: HistoryType,
        groupId: String,
        historyId: String
    ): Result<Unit>

    suspend fun clearCache()
}