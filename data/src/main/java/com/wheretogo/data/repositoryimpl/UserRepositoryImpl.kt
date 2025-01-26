package com.wheretogo.data.repositoryimpl

import android.util.Log
import com.wheretogo.data.datasource.UserLocalDatasource
import com.wheretogo.data.datasource.UserRemoteDatasource
import com.wheretogo.data.model.history.RemoteHistoryGroupWrapper
import com.wheretogo.domain.HistoryType
import com.wheretogo.domain.UserNotExistException
import com.wheretogo.domain.model.map.History
import com.wheretogo.domain.model.user.Profile
import com.wheretogo.domain.model.user.ProfilePrivate
import com.wheretogo.domain.model.user.ProfilePublic
import com.wheretogo.domain.model.user.SignResponse
import com.wheretogo.domain.repository.UserRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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

    override suspend fun getHistoryIdStream(type: HistoryType): Flow<HashSet<String>> {
        return userLocalDatasource.getHistoryFlow(type)
    }

    override suspend fun getProfileStream(): Flow<Profile> {
        return userLocalDatasource.getProfileFlow()
    }

    override suspend fun getPublicProfile(userId: String): ProfilePublic? {
        userId.isEmpty().let { if (it) throw UserNotExistException("inValid userId: $userId") }
        return userRemoteDatasource.getProfilePublic(userId)
    }

    override suspend fun getProfilePrivate(userId: String): ProfilePrivate? {
        return userRemoteDatasource.getProfilePrivate(userId)
    }

    override suspend fun setProfile(profile: Profile): Boolean {
        val userId = profile.uid
        userId.isEmpty().let { if (it) throw UserNotExistException("inValid userId: $userId") }
        if (userRemoteDatasource.setProfilePrivate(profile.uid, profile.private)
            && userRemoteDatasource.setProfilePublic(profile.uid, profile.public)
        ) {
            userLocalDatasource.setProfile(profile)
            return true
        }
        return false
    }

    private suspend fun getRemoteHistory(userId: String): History {
        var history = History()
        coroutineScope {
            HistoryType.entries.map {
                async {
                    userRemoteDatasource.getHistoryGroup(userId, it)
                }
            }.awaitAll()
        }.forEach {
            history = when (it.first) {
                HistoryType.COURSE -> {  history.copy(courseGroup = it.second) }
                HistoryType.CHECKPOINT -> {  history.copy(checkpointGroup = it.second) }
                HistoryType.COMMENT -> {  history.copy(commentGroup = it.second) }
                HistoryType.REPORT -> {  history.copy(reportGroup = it.second) }
                HistoryType.LIKE -> {  history.copy(likeGroup = it.second) }
                HistoryType.BOOKMARK -> {  history.copy(bookmarkGroup = it.second) }
            }
        }
        return history
    }

    private suspend fun setLocalHistory(history: History) {
        userLocalDatasource.setHistory(history)
    }

    override suspend fun signUp(profile: Profile): SignResponse {
        return if (setProfile(profile)) {
            SignResponse(SignResponse.Status.Success, profile)
        } else {
            SignResponse(SignResponse.Status.Fail)
        }
    }

    override suspend fun signIn(uid: String): SignResponse {
        return coroutineScope {
            val public = async { userRemoteDatasource.getProfilePublic(uid) }
            val private = async { userRemoteDatasource.getProfilePrivate(uid) }
            val history = async { getRemoteHistory(uid) }
            val newProfile = Profile(
                uid = uid,
                public = public.await() ?: return@coroutineScope SignResponse(
                    status = SignResponse.Status.Fail,
                    msg = "프로필 로드 에러 [public]"
                ),
                private = private.await() ?: return@coroutineScope SignResponse(
                    status = SignResponse.Status.Fail,
                    msg = "프로필 로드 에러 [private]"
                )
            )
            setProfile(newProfile)
            setLocalHistory(history.await())
            SignResponse(SignResponse.Status.Success, newProfile)
        }
    }


    override suspend fun signOut(): SignResponse {
        userLocalDatasource.clearUser()
        return SignResponse(SignResponse.Status.Success)
    }

    override suspend fun deleteUser(userId: String): Boolean {
        return if (userRemoteDatasource.deleteProfile(userId)) {
            signOut()
            true
        } else
            false
    }
}