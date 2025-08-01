package com.wheretogo.data.repositoryimpl

import com.wheretogo.data.datasource.UserLocalDatasource
import com.wheretogo.data.datasource.UserRemoteDatasource
import com.wheretogo.data.model.history.RemoteHistoryGroupWrapper
import com.wheretogo.data.toLocalProfile
import com.wheretogo.data.toProfile
import com.wheretogo.data.toProfilePrivate
import com.wheretogo.data.toProfilePublic
import com.wheretogo.data.toRemoteProfilePrivate
import com.wheretogo.domain.HistoryType
import com.wheretogo.domain.UserNotExistException
import com.wheretogo.domain.feature.hashSha256
import com.wheretogo.domain.model.map.History
import com.wheretogo.domain.model.user.AuthProfile
import com.wheretogo.domain.model.user.Profile
import com.wheretogo.domain.model.user.ProfilePrivate
import com.wheretogo.domain.repository.UserRepository
import com.wheretogo.domain.toHistory
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userLocalDatasource: UserLocalDatasource,
    private val userRemoteDatasource: UserRemoteDatasource
) : UserRepository {


    override fun isRequestLoginStream(): Flow<Boolean> {
        return userLocalDatasource.isRequestLoginFlow()
    }

    override suspend fun setRequestLogin(boolean: Boolean) {
        userLocalDatasource.setRequestLogin(boolean)
    }

    override suspend fun addHistory(
        userId: String,
        historyId: String,
        type: HistoryType
    ): Result<Unit> {
        return runCatching {
            userId.isBlank().let { if (it) throw UserNotExistException("inValid userId: $userId") }
            require(historyId.isNotEmpty()) { "inValid historyId: $historyId" }
            userLocalDatasource.addHistory(historyId, type)
            userRemoteDatasource.addHistory(
                uid = userId,
                historyId = historyId,
                type = type
            )
        }.onFailure {
            userLocalDatasource.removeHistory(historyId, type)
        }
    }

    override suspend fun setHistoryGroup(
        userId: String,
        historyGroup: HashSet<String>,
        type: HistoryType
    ): Result<Unit> {
        return runCatching {
            userId.isBlank().let { if (it) throw UserNotExistException("inValid userId: $userId") }
            historyGroup.forEach {
                userLocalDatasource.addHistory(it, type)
            }
            val wrapper = RemoteHistoryGroupWrapper(
                userLocalDatasource.getHistoryFlow(type).first().toList(),
                type
            )
            userRemoteDatasource.setHistoryGroup(userId, wrapper)
        }
    }

    override suspend fun removeHistory(
        userId: String,
        historyId: String,
        type: HistoryType
    ): Result<Unit> {
        return runCatching {
            userId.isBlank().let { if (it) throw UserNotExistException("inValid userId: $userId") }
            require(historyId.isNotEmpty()) { "inValid historyId: $historyId" }
            userLocalDatasource.removeHistory(historyId, type)
            userRemoteDatasource.setHistoryGroup(
                uid = getProfileStream().first().uid,
                wrapper = RemoteHistoryGroupWrapper(
                    getHistoryIdStream(type).first().toList(),
                    type
                )
            )
        }.onFailure {
            userLocalDatasource.addHistory(historyId, type)
        }
    }

    override suspend fun removeHistoryGroup(
        userId: String,
        historyIdGroup: HashSet<String>,
        type: HistoryType
    ): Result<Unit> {
        return runCatching {
            userId.isBlank().let { if (it) throw UserNotExistException("inValid userId: $userId") }
            val newHistoryIdGroup = userLocalDatasource.getHistoryFlow(type).first().filter {
                require(it.isNotEmpty()) { "inValid historyId: $historyIdGroup" }
                it !in historyIdGroup
            }
            userRemoteDatasource.setHistoryGroup(
                uid = getProfileStream().first().uid,
                wrapper = RemoteHistoryGroupWrapper(
                    newHistoryIdGroup,
                    type
                )
            )
            userLocalDatasource.setHistoryGroup(newHistoryIdGroup.toHashSet(), type)
        }
    }

    override suspend fun getHistoryIdStream(type: HistoryType): Flow<HashSet<String>> {
        return userLocalDatasource.getHistoryFlow(type)
    }

    override suspend fun getProfileStream(): Flow<Profile> {
        return userLocalDatasource.getProfileFlow().map { it.toProfile() }
    }

    override suspend fun getProfile(userId: String): Result<Profile> {
        return runCatching {
            coroutineScope {
                userId.isBlank()
                    .let { if (it) throw UserNotExistException("inValid userId: $userId") }
                val public = async { userRemoteDatasource.getProfilePublic(userId) }
                val private = async { userRemoteDatasource.getProfilePrivate(userId)}
                public.await()?.toProfile()?.copy(
                    private =
                        private.await()?.toProfilePrivate()
                            ?: throw UserNotExistException("inValid private userId: $userId")
                )
                    ?: throw UserNotExistException("inValid public userId: $userId")
            }
        }
    }

    override suspend fun getProfilePrivate(userId: String): Result<ProfilePrivate> {
        return runCatching {
            userRemoteDatasource.getProfilePrivate(userId)?.toProfilePrivate()
                ?: throw UserNotExistException("inValid userId: $userId")
        }
    }

    override suspend fun setProfile(profile: Profile): Result<Unit> {
        return runCatching {
            val userId = profile.uid
            userId.isBlank().let { if (it) throw UserNotExistException("inValid userId: $userId") }
            userRemoteDatasource.setProfilePublic(profile.toProfilePublic())
            userRemoteDatasource.setProfilePrivate(profile.uid, profile.private.toRemoteProfilePrivate())
            userLocalDatasource.setProfile(profile.toLocalProfile())
        }
    }

    private suspend fun getRemoteHistory(userId: String): Result<History> {
        return runCatching {
            coroutineScope {
                HistoryType.entries.map {
                    async {
                        userRemoteDatasource.getHistoryGroup(userId, it)
                    }
                }.awaitAll()
            }.toHistory()
        }
    }

    private suspend fun setLocalHistory(history: History) {
        userLocalDatasource.setHistory(history)
    }

    override suspend fun createUser(profile: Profile): Result<Unit> {
        return setProfile(profile)
    }

    override suspend fun syncUser(authProfile: AuthProfile): Result<Profile> {
        return runCatching {
            getProfile(authProfile.uid).onSuccess { profile->
              return@runCatching  coroutineScope {
                    val newPrivate =
                        async {
                            profile.private.copy(lastVisited = System.currentTimeMillis()).apply {
                                userRemoteDatasource.setProfilePrivate(
                                    uid = authProfile.uid,
                                    privateProfile = this.toRemoteProfilePrivate())
                            }
                        }

                    val newProfile = profile.copy(private = newPrivate.await())
                    val history = async { getRemoteHistory(authProfile.uid).getOrNull() ?: History() }
                    userLocalDatasource.setProfile(newProfile.toLocalProfile())
                    userLocalDatasource.setToken(authProfile.token)
                    setLocalHistory(history.await())
                    return@coroutineScope profile
                }
            }.onFailure {
                throw it
            }
            throw UserNotExistException("profile not found uid:${authProfile.uid}")
        }
    }


    override suspend fun clearUserCache() {
        userLocalDatasource.clearUser()
    }

    override suspend fun deleteUser(): Result<Unit> {
        return runCatching{
            val uid = userLocalDatasource.getProfileFlow().first().uid
            val token= userLocalDatasource.getTokenFlow().first()
            check(uid.isNotBlank())
            check(token.isNotBlank())
            userRemoteDatasource.deleteUser(uid, token)
            clearUserCache()
        }
    }

    override suspend fun checkUser(mail: String): Result<Profile> {
        return runCatching {
            val hashMail = hashSha256(mail)
            val public = userRemoteDatasource.getProfilePublicWithMail(hashMail)

            public?.toProfile()?:throw UserNotExistException("profile not found mail:$mail")
        }
    }
}