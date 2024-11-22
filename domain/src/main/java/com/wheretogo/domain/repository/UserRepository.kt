package com.wheretogo.domain.repository

import com.wheretogo.domain.model.user.Profile
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun isRequestLoginFlow(): Flow<Boolean>

    suspend fun setRequestLogin(boolean: Boolean)

    suspend fun addBookmark(code: Int)

    suspend fun removeBookmark(code: Int)

    suspend fun getBookmarkFlow(): Flow<List<Int>>

    suspend fun setProfile(profile: Profile)

    fun getProfileFlow(): Flow<Profile>
}