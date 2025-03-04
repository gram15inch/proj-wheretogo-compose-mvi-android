package com.wheretogo.data.repositoryimpl

import com.wheretogo.data.datasource.UserLocalDatasource
import com.wheretogo.data.datasource.UserRemoteDatasource
import com.wheretogo.data.model.history.RemoteHistoryGroupWrapper
import com.wheretogo.data.toProfile
import com.wheretogo.data.toProfilePublic
import com.wheretogo.domain.HistoryType
import com.wheretogo.domain.UserNotExistException
import com.wheretogo.domain.feature.hashSha256
import com.wheretogo.domain.model.map.History
import com.wheretogo.domain.model.user.Profile
import com.wheretogo.domain.model.user.ProfilePrivate
import com.wheretogo.domain.model.user.UserResponse
import com.wheretogo.domain.repository.UserRepository
import com.wheretogo.domain.toHistory
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userLocalDatasource: UserLocalDatasource,
    private val userRemoteDatasource: UserRemoteDatasource
) : UserRepository {


    override suspend fun isRequestLoginStream(): Flow<Boolean> {
        return userLocalDatasource.isRequestLoginFlow()
    }

    override suspend fun setRequestLogin(boolean: Boolean) {
        userLocalDatasource.setRequestLogin(boolean)
    }

    override suspend fun addHistory(
        userId: String,
        historyId: String,
        type: HistoryType
    ) {
        userId.isEmpty().let { if (it) throw UserNotExistException("inValid userId: $userId") }
        require(historyId.isNotEmpty()) { "inValid historyId: $historyId" }
        userLocalDatasource.addHistory(historyId, type)
        userRemoteDatasource.addHistory(
            uid = userId,
            historyId = historyId,
            type = type
        )
    }

    override suspend fun setHistoryGroup(
        userId: String,
        historyGroup: HashSet<String>,
        type: HistoryType
    ) {
        userId.isEmpty().let { if (it) throw UserNotExistException("inValid userId: $userId") }
        historyGroup.forEach {
            userLocalDatasource.addHistory(it, type)
        }
        val wrapper = RemoteHistoryGroupWrapper(
            userLocalDatasource.getHistoryFlow(type).first().toList(),
            type
        )
        userRemoteDatasource.setHistoryGroup(userId, wrapper)
    }

    override suspend fun removeHistory(
        userId: String,
        historyId: String,
        type: HistoryType
    ) {
        userId.isEmpty().let { if (it) throw UserNotExistException("inValid userId: $userId") }
        require(historyId.isNotEmpty()) { "inValid historyId: $historyId" }
        userLocalDatasource.removeHistory(historyId, type)
        userRemoteDatasource.setHistoryGroup(
            uid = getProfileStream().first().uid,
            wrapper = RemoteHistoryGroupWrapper(
                getHistoryIdStream(type).first().toList(),
                type
            )
        )
    }

    override suspend fun removeHistoryGroup(
        userId: String,
        historyIdGroup: HashSet<String>,
        type: HistoryType
    ) {
        userId.isEmpty().let { if (it) throw UserNotExistException("inValid userId: $userId") }
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

    override suspend fun getHistoryIdStream(type: HistoryType): Flow<HashSet<String>> {
        return userLocalDatasource.getHistoryFlow(type)
    }

    override suspend fun getProfileStream(): Flow<Profile> {
        return userLocalDatasource.getProfileFlow()
    }

    override suspend fun getProfile(userId: String): Profile? {
        userId.isEmpty().let { if (it) throw UserNotExistException("inValid userId: $userId") }
        return coroutineScope {
            val public = async { userRemoteDatasource.getProfilePublic(userId) }
            val private = async { userRemoteDatasource.getProfilePrivate(userId) ?: ProfilePrivate() }
            public.await()?.toProfile()?.copy(private = private.await())
        }
    }

    override suspend fun getProfilePrivate(userId: String): ProfilePrivate? {
        return userRemoteDatasource.getProfilePrivate(userId)
    }

    override suspend fun setProfile(profile: Profile): Boolean {
        val userId = profile.uid
        userId.isEmpty().let { if (it) throw UserNotExistException("inValid userId: $userId") }
        if (userRemoteDatasource.setProfilePublic(profile.toProfilePublic())
            && userRemoteDatasource.setProfilePrivate(profile.uid, profile.private)
        ) {
            userLocalDatasource.setProfile(profile)
            return true
        }
        return false
    }

    private suspend fun getRemoteHistory(userId: String): History {
        return coroutineScope {
            HistoryType.entries.map {
                async {
                    userRemoteDatasource.getHistoryGroup(userId, it)
                }
            }.awaitAll()
        }.toHistory()
    }

    private suspend fun setLocalHistory(history: History) {
        userLocalDatasource.setHistory(history)
    }

    override suspend fun createUser(profile: Profile): UserResponse {
        return if (setProfile(profile)) {
            UserResponse(UserResponse.Status.Success, profile)
        } else {
            UserResponse(UserResponse.Status.Fail)
        }
    }

    override suspend fun syncUser(uid: String): UserResponse {
        return coroutineScope {
            val profile = getProfile(uid)
                ?: return@coroutineScope UserResponse(
                    status = UserResponse.Status.Fail,
                    msg = "프로필 로드 에러"
                )
            val newPrivate = profile.private.copy(lastVisited = System.currentTimeMillis()).apply {
                launch { userRemoteDatasource.setProfilePrivate(uid, this@apply) }
            }
            val newProfile = profile.copy(private = newPrivate)
            val history = async { getRemoteHistory(uid) }
            userLocalDatasource.setProfile(newProfile)
            setLocalHistory(history.await())
            UserResponse(UserResponse.Status.Success, profile)
        }
    }


    override suspend fun clearUser(): UserResponse {
        userLocalDatasource.clearUser()
        return UserResponse(UserResponse.Status.Success)
    }

    override suspend fun deleteUser(userId: String): Boolean {
        return if (userRemoteDatasource.deleteProfile(userId)) {
            clearUser()
            true
        } else
            false
    }

    override suspend fun checkUser(mail: String): UserResponse {
        val hashMail = hashSha256(mail)
        val public = userRemoteDatasource.getProfilePublicWithMail(hashMail)
        return if (public != null)
            UserResponse(UserResponse.Status.Success, public.toProfile())
        else
            UserResponse(UserResponse.Status.Fail, msg = "프로필 없음")
    }
}