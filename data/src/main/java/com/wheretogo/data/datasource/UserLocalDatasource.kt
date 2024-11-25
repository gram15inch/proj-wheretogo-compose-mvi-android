package com.wheretogo.data.datasource

import com.wheretogo.domain.model.user.Profile
import kotlinx.coroutines.flow.Flow

interface UserLocalDatasource {
    fun isRequestLoginFlow(): Flow<Boolean>

    suspend fun setRequestLogin(boolean: Boolean)

    suspend fun addBookmark(code: Int)

    suspend fun removeBookmark(code: Int)

    fun getBookmarkFlow(): Flow<List<Int>>

    suspend fun setProfile(profile: Profile)

    suspend fun clearUser()

    fun getProfileFlow(): Flow<Profile>

}