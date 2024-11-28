package com.wheretogo.data.repositoryimpl

import com.wheretogo.data.datasource.UserLocalDatasource
import com.wheretogo.data.datasource.UserRemoteDatasource
import com.wheretogo.domain.model.user.Profile
import com.wheretogo.domain.model.user.SignResponse
import com.wheretogo.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userLocalDatasource: UserLocalDatasource,
    private val userRemoteDatasource: UserRemoteDatasource
) :
    UserRepository {

    override suspend fun isRequestLoginFlow(): Flow<Boolean> {
        return userLocalDatasource.isRequestLoginFlow()
    }

    override suspend fun setRequestLogin(boolean: Boolean) {
        userLocalDatasource.setRequestLogin(boolean)
    }

    override suspend fun addBookmark(code: Int) {
        userLocalDatasource.addBookmark(code)
    }

    override suspend fun removeBookmark(code: Int) {
        userLocalDatasource.removeBookmark(code)
    }

    override suspend fun getBookmarkFlow(): Flow<List<Int>> {
        return userLocalDatasource.getBookmarkFlow()
    }

    override suspend fun getProfileFlow(): Flow<Profile> {
        return userLocalDatasource.getProfileFlow()
    }

    override suspend fun isUserExists(uid: String): Boolean {
        return userRemoteDatasource.getProfile(uid) != null
    }

    override suspend fun signUp(profile: Profile): SignResponse {
        return try {
            if (userRemoteDatasource.setProfile(profile)) {
                SignResponse(SignResponse.Status.Success)
            } else {
                SignResponse(SignResponse.Status.Fail)
            }
        } catch (e: Exception) {
            SignResponse(SignResponse.Status.Error)
        }
    }

    override suspend fun setProfile(profile: Profile): Boolean {
        if (userRemoteDatasource.setProfile(profile)) {
            userLocalDatasource.setProfile(profile)
            return true
        }
        return false
    }

    override suspend fun signIn(uid: String): SignResponse {
        return try {
            val profile = userRemoteDatasource.getProfile(uid)
            if (profile != null) {
                userLocalDatasource.setProfile(profile)
                SignResponse(SignResponse.Status.Success)
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
            SignResponse(SignResponse.Status.Success)
        } catch (e: Exception) {
            SignResponse(SignResponse.Status.Error)
        }
    }
}