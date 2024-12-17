package com.wheretogo.data.repositoryimpl

import com.wheretogo.data.datasource.UserLocalDatasource
import com.wheretogo.data.datasource.UserRemoteDatasource
import com.wheretogo.data.model.comment.RemoteBookmarkGroupWrapper
import com.wheretogo.data.model.comment.RemoteLikeGroupWrapper
import com.wheretogo.domain.HistoryType
import com.wheretogo.domain.model.user.Profile
import com.wheretogo.domain.model.user.SignResponse
import com.wheretogo.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userLocalDatasource: UserLocalDatasource,
    private val userRemoteDatasource: UserRemoteDatasource
) :
    UserRepository {

    override suspend fun isRequestLoginStream(): Flow<Boolean> {
        return userLocalDatasource.isRequestLoginFlow()
    }

    override suspend fun setRequestLogin(boolean: Boolean) {
        userLocalDatasource.setRequestLogin(boolean)
    }

    override suspend fun addBookmark(code: String) {
        userLocalDatasource.addHistory(code, HistoryType.BOOKMARK)
        userRemoteDatasource.setBookmarkGroup(
            wrapper = RemoteBookmarkGroupWrapper(
                getProfileStream().first().uid,
                getBookmarkStream().first()
            )
        )
    }

    override suspend fun addBookmarkGroup(uId: String, bookmarkGroup: List<String>) {
        bookmarkGroup.forEach {
            userLocalDatasource.addHistory(it, HistoryType.BOOKMARK)
        }

        val wrapper = RemoteBookmarkGroupWrapper(
            uId,
            userLocalDatasource.getHistoryFlow(HistoryType.BOOKMARK).first()
        )
        userRemoteDatasource.setBookmarkGroup(wrapper)
    }

    override suspend fun removeBookmark(code: String) {
        userLocalDatasource.removeHistory(code, HistoryType.BOOKMARK)
        userRemoteDatasource.setBookmarkGroup(
            wrapper = RemoteBookmarkGroupWrapper(
                getProfileStream().first().uid,
                getBookmarkStream().first()
            )
        )
    }

    override suspend fun getBookmarkStream(): Flow<List<String>> {
        return userLocalDatasource.getHistoryFlow(HistoryType.BOOKMARK)
    }

    override suspend fun addLike(code: String) {
        userLocalDatasource.addHistory(code, HistoryType.LIKE)
        userRemoteDatasource.setLikeGroup(
            wrapper = RemoteLikeGroupWrapper(
                getProfileStream().first().uid,
                getLikeStream().first()
            )
        )
    }

    override suspend fun addLikeGroup(uId: String, likeGroup: List<String>) {
        likeGroup.forEach {
            userLocalDatasource.addHistory(it, HistoryType.LIKE)
        }
        val wrapper = RemoteLikeGroupWrapper(
            uId,
            userLocalDatasource.getHistoryFlow(HistoryType.LIKE).first()
        )
        userRemoteDatasource.setLikeGroup(wrapper)
    }

    override suspend fun removeLike(code: String) {
        userLocalDatasource.removeHistory(code, HistoryType.LIKE)
        userRemoteDatasource.setLikeGroup(
            wrapper = RemoteLikeGroupWrapper(
                getProfileStream().first().uid,
                getLikeStream().first()
            )
        )
    }

    override suspend fun getLikeStream(): Flow<List<String>> {
        return userLocalDatasource.getHistoryFlow(HistoryType.LIKE)
    }

    override suspend fun getProfileStream(): Flow<Profile> {
        return userLocalDatasource.getProfileFlow()
    }

    override suspend fun getProfile(uid: String): Profile? {
        return userRemoteDatasource.getProfile(uid)
    }

    override suspend fun setProfile(profile: Profile): Boolean {
        if (userRemoteDatasource.setProfile(profile)) {
            userLocalDatasource.setProfile(profile)
            return true
        }
        return false
    }

    override suspend fun signUp(profile: Profile): SignResponse {
        return try {
            if (userRemoteDatasource.setProfile(profile)) {
                SignResponse(SignResponse.Status.Success, profile)
            } else {
                SignResponse(SignResponse.Status.Fail)
            }
        } catch (e: Exception) {
            SignResponse(SignResponse.Status.Error)
        }
    }

    override suspend fun signIn(uid: String): SignResponse {
        return try {
            val profile = userRemoteDatasource.getProfileWithLikeAndBookmark(uid)
            if (profile != null) {
                userLocalDatasource.setProfile(profile)
                SignResponse(SignResponse.Status.Success, profile)
            } else {
                SignResponse(SignResponse.Status.Fail)
            }
        } catch (e: Exception) {
            SignResponse(SignResponse.Status.Error)
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