package com.wheretogo.domain.repository

import com.wheretogo.domain.HistoryType
import com.wheretogo.domain.model.history.HistoryGroupWrapper
import com.wheretogo.domain.model.user.History
import com.wheretogo.domain.model.user.Profile
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getProfileStream(): Flow<Profile>

    suspend fun cacheUser(profile: Profile, history: History): Result<Unit>

    suspend fun deleteUser(): Result<Unit>

    suspend fun updateMsgToken(token: String): Result<Unit>

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