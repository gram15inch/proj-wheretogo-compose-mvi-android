package com.wheretogo.domain.repository

import com.wheretogo.domain.model.user.Profile
import com.wheretogo.domain.model.user.SignResponse
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun isRequestLoginStream(): Flow<Boolean>

    suspend fun setRequestLogin(boolean: Boolean)

    suspend fun addBookmark(code: String)

    suspend fun addBookmarkGroup(uId: String, bookmarkGroup: List<String>)

    suspend fun removeBookmark(code: String)

    suspend fun getBookmarkStream(): Flow<List<String>>

    suspend fun addLike(code: String)

    suspend fun addLikeGroup(uId: String, likeGroup: List<String>)

    suspend fun removeLike(code: String)

    suspend fun getLikeStream(): Flow<List<String>>

    suspend fun getProfileStream(): Flow<Profile>

    suspend fun getProfile(uid: String): Profile?

    suspend fun setProfile(profile: Profile): Boolean

    suspend fun signUp(profile: Profile): SignResponse

    suspend fun signIn(uid: String): SignResponse

    suspend fun signOut(): SignResponse
}