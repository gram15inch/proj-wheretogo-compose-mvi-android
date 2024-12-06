package com.wheretogo.domain.repository

import com.wheretogo.domain.model.user.Profile
import com.wheretogo.domain.model.user.SignResponse
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun isRequestLoginFlow(): Flow<Boolean>

    suspend fun setRequestLogin(boolean: Boolean)

    suspend fun addBookmark(code: String)

    suspend fun removeBookmark(code: String)

    suspend fun getBookmarkFlow(): Flow<List<String>>

    suspend fun getProfileFlow(): Flow<Profile>

    suspend fun isUserExists(uid: String): Boolean

    suspend fun setProfile(profile: Profile): Boolean

    suspend fun signUp(profile: Profile): SignResponse

    suspend fun signIn(uid: String): SignResponse

    suspend fun signOut(): SignResponse
}