package com.dhkim139.wheretogo.mock

import com.dhkim139.wheretogo.mock.model.MockRemoteUser
import com.wheretogo.data.datasource.UserRemoteDatasource
import com.wheretogo.data.model.history.RemoteHistoryGroupWrapper
import com.wheretogo.domain.HistoryType
import com.wheretogo.domain.get
import com.wheretogo.domain.map
import com.wheretogo.domain.model.user.Profile
import com.wheretogo.domain.model.user.ProfilePrivate
import com.wheretogo.domain.model.user.ProfilePublic
import javax.inject.Inject

class MockUserRemoteDatasourceImpl @Inject constructor(
    private val mockRemoteUser: MockRemoteUser
) : UserRemoteDatasource {
    private var userRemoteGroup =
        mutableMapOf<String, MockRemoteUser>(mockRemoteUser.profile.uid to mockRemoteUser) // userId

    override suspend fun setProfilePublic(uid: String, publicPorfile: ProfilePublic): Boolean {
        val newUser = userRemoteGroup.getOrPut(uid) {
            MockRemoteUser(
                uid,
                Profile(uid, public = publicPorfile)
            )
        }.run {
            copy(profile = profile.copy(public = publicPorfile))
        }
        userRemoteGroup.put(uid, newUser)
        return true
    }

    override suspend fun setProfilePrivate(uid: String, privateProfile: ProfilePrivate): Boolean {
        val newUser = userRemoteGroup.getOrPut(uid) {
            MockRemoteUser(
                uid,
                Profile(uid, private = privateProfile)
            )
        }.run {
            copy(profile = profile.copy(private = privateProfile))
        }
        userRemoteGroup.put(uid, newUser)
        return true
    }

    override suspend fun getProfilePublic(uid: String): ProfilePublic? {
        return userRemoteGroup.get(uid)?.profile?.public
    }

    override suspend fun getProfilePrivate(uid: String): ProfilePrivate? {
        return userRemoteGroup.get(uid)?.profile?.private
    }

    override suspend fun deleteProfile(uid: String): Boolean {
        userRemoteGroup.remove(uid)
        return true
    }

    override suspend fun addHistory(uid: String, historyId: String, type: HistoryType): Boolean {
        val user = userRemoteGroup.get(uid)
        return if (user != null) {
            val newContent = (user.history.get(type) + historyId).toHashSet()
            val newHistory = user.history.map(type, newContent)
            val newUser = user.copy(history = newHistory)
            userRemoteGroup[uid] = newUser
            true
        } else {
            false
        }
    }

    override suspend fun getHistoryGroup(
        uid: String,
        type: HistoryType
    ): Pair<HistoryType, HashSet<String>> {
        return (type to userRemoteGroup.get(uid)?.history?.get(type) as HashSet)
    }

    override suspend fun setHistoryGroup(uid: String, wrapper: RemoteHistoryGroupWrapper): Boolean {
        val user = userRemoteGroup.get(uid)
        return if (user != null) {
            val newUser = user.copy(
                history = user.history.map(
                    wrapper.type,
                    wrapper.historyIdGroup.toHashSet()
                )
            )
            userRemoteGroup[uid] = newUser
            true
        } else {
            false
        }
    }
}