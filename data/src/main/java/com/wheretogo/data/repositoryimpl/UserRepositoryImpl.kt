package com.wheretogo.data.repositoryimpl

import com.wheretogo.data.datasource.UserLocalDatasource
import com.wheretogo.data.datasource.UserRemoteDatasource
import com.wheretogo.data.model.history.RemoteHistoryGroupWrapper
import com.wheretogo.domain.HistoryType
import com.wheretogo.domain.model.user.Profile
import com.wheretogo.domain.model.user.ProfilePrivate
import com.wheretogo.domain.model.user.ProfilePublic
import com.wheretogo.domain.model.user.SignResponse
import com.wheretogo.domain.repository.UserRepository
import kotlinx.coroutines.async
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

    override suspend fun addHistory(historyId: String, type: HistoryType) {
        userLocalDatasource.addHistory(historyId, type)
        userRemoteDatasource.addHistory(
            uid = getProfileStream().first().uid,
            historyId = historyId,
            type = type

        )
    }

    override suspend fun setHistoryGroup(
        uId: String,
        historyGroup: HashSet<String>,
        type: HistoryType
    ) {
        historyGroup.forEach {
            userLocalDatasource.addHistory(it, type)
        }

        val wrapper = RemoteHistoryGroupWrapper(
            userLocalDatasource.getHistoryFlow(type).first().toList(),
            type
        )
        userRemoteDatasource.setHistoryGroup(uId, wrapper)
    }

    override suspend fun removeHistory(id: String, type: HistoryType) {
        userLocalDatasource.removeHistory(id, type)
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

    override suspend fun getPublicProfile(uid: String): ProfilePublic? {
        return userRemoteDatasource.getProfilePublic(uid)
    }

    override suspend fun getProfilePrivate(uid: String): ProfilePrivate? {
        return userRemoteDatasource.getProfilePrivate(uid)
    }

    override suspend fun setProfile(profile: Profile): Boolean {
        if (userRemoteDatasource.setProfilePrivate(profile.uid, profile.private)
            && userRemoteDatasource.setProfilePublic(profile.uid, profile.public)
        ) {
            userLocalDatasource.setProfile(profile)
            return true
        }
        return false
    }

    override suspend fun signUp(profile: Profile): SignResponse {
        return try {
            if (setProfile(profile)) {
                SignResponse(SignResponse.Status.Success, profile)
            } else {
                SignResponse(SignResponse.Status.Fail)
            }
        } catch (e: Exception) {
            SignResponse(SignResponse.Status.Error, msg = e.message ?: "")
        }
    }

    override suspend fun signIn(uid: String): SignResponse {
        return try {
            val profile = coroutineScope {
                val public = async { userRemoteDatasource.getProfilePublic(uid) }
                val private = async { userRemoteDatasource.getProfilePrivate(uid) }
                Profile(
                    uid,
                    public = public.await() ?: return@coroutineScope null,
                    private = private.await() ?: return@coroutineScope null
                )
            }

            if (profile != null) {
                setProfile(profile)
                SignResponse(SignResponse.Status.Success, profile)
            } else {
                SignResponse(SignResponse.Status.Fail)
            }
        } catch (e: Exception) {
            SignResponse(SignResponse.Status.Error, msg = e.message ?: "")
        }
    }

    override suspend fun signOut(): SignResponse {
        return try {
            userLocalDatasource.clearUser()
            userLocalDatasource.clearHistory()
            SignResponse(SignResponse.Status.Success)
        } catch (e: Exception) {
            SignResponse(SignResponse.Status.Error)
        }
    }
}