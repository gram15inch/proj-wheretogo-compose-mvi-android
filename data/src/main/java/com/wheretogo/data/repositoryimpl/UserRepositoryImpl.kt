package com.wheretogo.data.repositoryimpl

import com.wheretogo.data.DataError
import com.wheretogo.data.datasource.UserLocalDatasource
import com.wheretogo.data.datasource.UserRemoteDatasource
import com.wheretogo.data.feature.dataErrorCatching
import com.wheretogo.data.feature.mapDomainError
import com.wheretogo.data.feature.mapSuccess
import com.wheretogo.data.model.history.RemoteHistoryGroupWrapper
import com.wheretogo.data.toLocalProfile
import com.wheretogo.data.toProfile
import com.wheretogo.data.toProfilePrivate
import com.wheretogo.data.toProfilePublic
import com.wheretogo.data.toRemoteProfilePrivate
import com.wheretogo.domain.HistoryType
import com.wheretogo.domain.feature.hashSha256
import com.wheretogo.domain.feature.zip
import com.wheretogo.domain.model.user.AuthProfile
import com.wheretogo.domain.model.user.History
import com.wheretogo.domain.model.user.Profile
import com.wheretogo.domain.repository.UserRepository
import com.wheretogo.domain.toHistory
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userLocalDatasource: UserLocalDatasource,
    private val userRemoteDatasource: UserRemoteDatasource
) : UserRepository {

    override suspend fun getProfileStream(): Flow<Profile> {
        return userLocalDatasource.getProfileFlow().map { it.toProfile() }
    }

    override suspend fun createUser(profile: Profile): Result<Unit> {
        return runCatching {
            val userId = profile.uid
            userId.isBlank().let { if (it) throw DataError.UserInvalid() }
        }.mapSuccess {
            val remotePublic = profile.toProfilePublic()
            val remotePrivate = profile.private.toRemoteProfilePrivate()
            val local = remotePublic.toLocalProfile(remotePrivate)
            userRemoteDatasource.setProfilePublic(remotePublic).zip(
                userRemoteDatasource.setProfilePrivate(
                    profile.uid,
                    remotePrivate
                )
            ) { _, _ -> userLocalDatasource.setProfile(local) }
                .mapCatching { }
        }.mapDomainError()
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

    override suspend fun syncUser(authProfile: AuthProfile): Result<Profile> {
        return getRemoteProfile(authProfile.uid)
            .mapSuccess { profile ->
                coroutineScope {
                    val profileResult =
                        async {
                            userRemoteDatasource.setProfilePrivate(
                                uid = authProfile.uid,
                                privateProfile = profile.private.toRemoteProfilePrivate()
                            )
                        }

                    val historyResult =
                        async { getRemoteHistory(authProfile.uid) }

                    val newProfile = profile.copy(
                        private = profile.private.copy(lastVisited = System.currentTimeMillis())
                    )
                    profileResult.await().zip(historyResult.await(), { _, history ->
                        userLocalDatasource.setProfile(newProfile.toLocalProfile()).mapSuccess {
                            userLocalDatasource.setHistory(history)
                        }
                    }).mapCatching { newProfile }
                }
            }.mapDomainError()
    }

    override suspend fun clearCache() {
        userLocalDatasource.clearUser()
    }

    override suspend fun checkUser(mail: String): Result<Profile> {
        return runCatching {
            hashSha256(mail)
        }.mapSuccess { hashMail ->
            userRemoteDatasource.getProfilePublicWithMail(hashMail)
                .mapCatching { it.toProfile() }
        }.mapDomainError()
    }


    private suspend fun getRemoteProfile(userId: String): Result<Profile> {
        return coroutineScope {
            val public = async { userRemoteDatasource.getProfilePublic(userId) }
            val private = async { userRemoteDatasource.getProfilePrivate(userId) }

            public.await().zip(private.await(), { public, private ->
                public.toProfile().copy(
                    private = private.toProfilePrivate()
                )
            })
        }.mapDomainError()
    }


    // history

    override suspend fun getHistory(type: HistoryType): HashSet<String> {
        return userLocalDatasource.getHistoryFlow(type).first()
    }

    override suspend fun addHistory(
        historyId: String,
        type: HistoryType
    ): Result<Unit> {
        return dataErrorCatching {
            val profile = validateUser()
            require(historyId.isNotEmpty()) { "inValid historyId: $historyId" }
            profile
        }.mapSuccess { profile ->
            userRemoteDatasource.addHistory(profile.uid, historyId, type)
            userLocalDatasource.addHistory(historyId, type)
        }.onFailure {
            userLocalDatasource.removeHistory(historyId, type)
        }.mapDomainError()
    }

    override suspend fun addHistoryToLocal(
        historyId: String,
        type: HistoryType
    ): Result<Unit> {
        return dataErrorCatching {
            validateUser()
            require(historyId.isNotEmpty()) { "inValid historyId: $historyId" }
        }.mapSuccess {
            userLocalDatasource.addHistory(historyId, type)
        }.mapDomainError()
    }

    override suspend fun removeHistory(
        historyId: String,
        type: HistoryType
    ): Result<Unit> {
        return dataErrorCatching {
            val profile = validateUser()
            require(historyId.isNotEmpty()) { "inValid historyId: $historyId" }
            profile
        }.mapSuccess { profile ->
            userLocalDatasource.removeHistory(historyId, type)
            userRemoteDatasource.setHistoryGroup(
                uid = profile.uid,
                wrapper = RemoteHistoryGroupWrapper(
                    getHistory(type).toList(),
                    type
                )
            )
        }.onFailure {
            userLocalDatasource.addHistory(historyId, type)
        }
    }

    override suspend fun removeHistoryToLocal(
        historyId: String,
        type: HistoryType
    ): Result<Unit> {
        return dataErrorCatching {
            validateUser()
            require(historyId.isNotEmpty()) { "inValid historyId: $historyId" }
        }.mapSuccess {
            userLocalDatasource.removeHistory(historyId, type)
        }.mapDomainError()
    }

    private suspend fun getRemoteHistory(userId: String): Result<History> {
        return userRemoteDatasource.getHistoryGroup(userId).mapCatching { it.toHistory() }
            .mapDomainError()
    }

    private suspend fun validateUser(): Profile {
        val profile = userLocalDatasource.getProfileFlow().first()

        if (profile.uid.isBlank())
            throw DataError.UserInvalid("")

        return profile.toProfile()
    }
}