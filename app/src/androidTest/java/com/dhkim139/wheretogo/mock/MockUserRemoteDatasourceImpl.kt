package com.dhkim139.wheretogo.mock

import com.dhkim139.wheretogo.mock.model.MockRemoteUser
import com.wheretogo.data.datasource.UserRemoteDatasource
import com.wheretogo.data.model.history.RemoteHistoryGroupWrapper
import com.wheretogo.domain.HistoryType
import com.wheretogo.domain.model.user.ProfilePrivate
import com.wheretogo.domain.model.user.ProfilePublic
import javax.inject.Inject

class MockUserRemoteDatasourceImpl @Inject constructor(
    private val mockRemoteUser: MockRemoteUser
) : UserRemoteDatasource {
    private var newRemoteUser = mockRemoteUser
    override suspend fun setProfilePublic(uid: String, profile: ProfilePublic): Boolean {
        newRemoteUser =
            newRemoteUser.copy(profile = newRemoteUser.profile.copy(uid = uid, public = profile))
        return true
    }

    override suspend fun setProfilePrivate(uid: String, profile: ProfilePrivate): Boolean {
        newRemoteUser =
            newRemoteUser.copy(profile = newRemoteUser.profile.copy(uid = uid, private = profile))
        return true
    }

    override suspend fun getProfilePublic(uid: String): ProfilePublic? {
        return newRemoteUser.profile.public
    }

    override suspend fun getProfilePrivate(uid: String): ProfilePrivate? {
        return newRemoteUser.profile.private
    }

    override suspend fun deleteProfile(uid: String): Boolean {
        newRemoteUser = MockRemoteUser()
        return true
    }

    override suspend fun addHistory(uid: String, historyId: String, type: HistoryType): Boolean {
        newRemoteUser.copy(history = newRemoteUser.history.toMutableMap().apply {
            val newHistoryGroup = get(type)!!.toMutableList().apply {
                add(historyId)
            }
            put(type, newHistoryGroup.toHashSet())
        })
        return true
    }

    override suspend fun getHistoryGroup(uid: String, type: HistoryType): Pair<HistoryType, HashSet<String>> {
        return type to newRemoteUser.history.get(type)!!.toHashSet()
    }

    override suspend fun setHistoryGroup(uid: String, wrapper: RemoteHistoryGroupWrapper): Boolean {
        newRemoteUser.copy(history = newRemoteUser.history.toMutableMap().apply {
            put(wrapper.type, wrapper.historyIdGroup.toHashSet())
        })
        return true
    }
}