package com.wheretogo.data.datasource

import com.wheretogo.data.model.comment.RemoteBookmarkGroupWrapper
import com.wheretogo.data.model.comment.RemoteLikeGroupWrapper
import com.wheretogo.domain.model.user.Profile

interface UserRemoteDatasource {
    suspend fun setProfile(profile: Profile): Boolean
    suspend fun getProfile(uid: String): Profile?
    suspend fun getProfileWithLikeAndBookmark(uid: String): Profile?

    suspend fun getBookmarkGroup(uid: String): List<String>
    suspend fun setBookmarkGroup(wrapper: RemoteBookmarkGroupWrapper): Boolean
    suspend fun getLikeGroup(uid: String): List<String>
    suspend fun setLikeGroup(wrapper: RemoteLikeGroupWrapper): Boolean
}