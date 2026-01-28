package com.wheretogo.data.repositoryimpl

import com.wheretogo.data.DataError
import com.wheretogo.data.datasource.UserLocalDatasource
import com.wheretogo.data.datasource.UserRemoteDatasource
import com.wheretogo.data.feature.dataErrorCatching
import com.wheretogo.data.feature.mapDomainError
import com.wheretogo.data.feature.mapSuccess
import com.wheretogo.data.network.ServerMsg
import com.wheretogo.data.toDataHistoryType
import com.wheretogo.data.toDomainResult
import com.wheretogo.data.toHistory
import com.wheretogo.data.toHistoryGroupWrapper
import com.wheretogo.data.toLocalProfile
import com.wheretogo.data.toProfile
import com.wheretogo.domain.HistoryType
import com.wheretogo.domain.model.history.HistoryGroupWrapper
import com.wheretogo.domain.model.user.History
import com.wheretogo.domain.model.user.Profile
import com.wheretogo.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userLocalDatasource: UserLocalDatasource,
    private val userRemoteDatasource: UserRemoteDatasource,
) : UserRepository {

    override suspend fun getProfileStream(): Flow<Profile> {
        return userLocalDatasource.getProfileFlow().map { it.toProfile() }
    }

    override suspend fun deleteUser(): Result<Unit> {
        return runCatching {
            val uid = userLocalDatasource.getProfileFlow().first().uid
            check(uid.isNotBlank())
            uid
        }.mapSuccess { uid ->
            userRemoteDatasource.deleteUser(uid)
        }.mapCatching {
            clearCache()
        }.mapDomainError()
    }

    override suspend fun updateMsgToken(token: String): Result<Unit> {
        return userRemoteDatasource
            .updateMsgToken(token)
            .mapDomainError()
    }

    override suspend fun cacheUser(
        profile: Profile,
        history: History
    ): Result<Unit> {
        return userLocalDatasource.setProfile(profile.toLocalProfile()).mapSuccess {
            userLocalDatasource.iniHistory(history.toHistory())
        }.toDomainResult()
    }

    override suspend fun clearCache() {
        userLocalDatasource.clearUser()
    }

    // history
    override suspend fun getHistory(type: HistoryType): Result<HistoryGroupWrapper> {
        return dataErrorCatching {
            userLocalDatasource.getHistory(type.toDataHistoryType()).toHistoryGroupWrapper()
        }.mapDomainError()
    }

    override suspend fun addHistory(
        type: HistoryType,
        groupId:String,
        historyId: String
    ): Result<Unit> {
        return dataErrorCatching {
            val profile = validateUser()
            require(historyId.isNotEmpty()) { "inValid historyId: $historyId" }
            profile
        }.mapSuccess { profile ->
            userRemoteDatasource.addHistory(type.toDataHistoryType(), profile.uid, groupId, historyId)
        }.mapSuccess {
            userLocalDatasource.addHistory(type.toDataHistoryType(), groupId, historyId,it)
        }.mapDomainError()
    }

    override suspend fun addHistoryToLocal(
        type: HistoryType,
        groupId: String,
        historyId: String,
        addedAt: Long
    ): Result<Unit> {
        return dataErrorCatching {
            validateUser()
            require(historyId.isNotEmpty()) { "inValid historyId: $historyId" }
        }.mapSuccess {
            userLocalDatasource.addHistory(type.toDataHistoryType(), groupId, historyId, addedAt)
        }.mapDomainError()
    }

    override suspend fun removeHistory(
        type: HistoryType,
        groupId:String,
        historyId: String
    ): Result<Unit> {
        return dataErrorCatching {
            val profile = validateUser()
            require(historyId.isNotEmpty()) { "inValid historyId: $historyId" }
            profile
        }.mapSuccess { profile ->
            userLocalDatasource.removeHistory(type.toDataHistoryType(), groupId, historyId)
                .mapSuccess {
                    userRemoteDatasource.removeHistory(
                        type = type.toDataHistoryType(),
                        uid = profile.uid,
                        groupId = groupId,
                        historyId = historyId
                    )
                }
        }.mapDomainError()
    }

    override suspend fun removeHistoryToLocal(
        type: HistoryType,
        groupId: String,
        historyId: String
    ): Result<Unit> {
        return dataErrorCatching {
            validateUser()
            require(historyId.isNotEmpty()) { "inValid historyId: $historyId" }
        }.mapSuccess {
            userLocalDatasource.removeHistory(type.toDataHistoryType(), groupId, historyId)
        }.mapDomainError()
    }

    private suspend fun validateUser(): Profile {
        val profile = userLocalDatasource.getProfileFlow().first()

        if (profile.uid.isBlank())
            throw DataError.UserInvalid("")

        return profile.toProfile()
    }
}