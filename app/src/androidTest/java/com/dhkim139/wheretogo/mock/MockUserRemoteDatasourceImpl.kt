package com.dhkim139.wheretogo.mock

import com.dhkim139.wheretogo.mock.model.MockRemoteUser
import com.wheretogo.data.datasource.UserRemoteDatasource
import com.wheretogo.data.model.history.RemoteHistoryGroupWrapper
import com.wheretogo.data.model.user.RemoteProfilePrivate
import com.wheretogo.data.model.user.RemoteProfilePublic
import com.wheretogo.data.toProfile
import com.wheretogo.data.toProfilePrivate
import com.wheretogo.data.toProfilePublic
import com.wheretogo.data.toRemoteProfilePrivate
import com.wheretogo.domain.HistoryType
import com.wheretogo.domain.get
import com.wheretogo.domain.map
import com.wheretogo.domain.model.user.ProfilePrivate
import javax.inject.Inject

class MockUserRemoteDatasourceImpl @Inject constructor(
    private val mockRemoteUser: MockRemoteUser
) : UserRemoteDatasource {
    private var userRemoteGroup =
        mutableMapOf<String, MockRemoteUser>(mockRemoteUser.profile.uid to mockRemoteUser) // userId

    override suspend fun setProfilePublic(public: RemoteProfilePublic) {
        val uid = public.uid
        val newUser = userRemoteGroup.getOrPut(uid) {
            MockRemoteUser(
                uid,
                profile = public.toProfile()
            )
        }.run {
            copy(profile = profile)
        }
        userRemoteGroup.put(uid, newUser)
    }

    override suspend fun setProfilePrivate(uid: String, privateProfile: RemoteProfilePrivate) {
        val newUser = userRemoteGroup.getOrPut(uid) {
            MockRemoteUser(
                uid,
                getProfilePublic(uid)?.toProfile()?.copy(uid=uid,private = privateProfile.toProfilePrivate())?:return
            )
        }.run {
            copy(profile = profile.copy(private = privateProfile.toProfilePrivate()))
        }
        userRemoteGroup.put(uid, newUser)
    }

    override suspend fun getProfilePublic(uid: String): RemoteProfilePublic? {
        return userRemoteGroup.get(uid)?.profile?.toProfilePublic()
    }

    override suspend fun getProfilePrivate(uid: String): RemoteProfilePrivate? {
        return userRemoteGroup.get(uid)?.profile?.private?.toRemoteProfilePrivate()
    }

    override suspend fun deleteProfile(uid: String) {
        userRemoteGroup.remove(uid)
    }

    override suspend fun addHistory(uid: String, historyId: String, type: HistoryType) {
        val user = userRemoteGroup.get(uid)
        if (user != null) {
            val newContent = (user.history.get(type) + historyId).toHashSet()
            val newHistory = user.history.map(type, newContent)
            val newUser = user.copy(history = newHistory)
            userRemoteGroup[uid] = newUser
        }
    }

    override suspend fun deleteHistory(uid: String, type: HistoryType) {
        val user = userRemoteGroup.get(uid)
        if (user != null) {
            val newContent = hashSetOf<String>()
            val newHistory = user.history.map(type, newContent)
            val newUser = user.copy(history = newHistory)
            userRemoteGroup[uid] = newUser
        }
    }

    override suspend fun getHistoryGroup(
        uid: String,
        type: HistoryType
    ): Pair<HistoryType, HashSet<String>> {
        return (type to userRemoteGroup.get(uid)?.history?.get(type) as HashSet)
    }

    override suspend fun setHistoryGroup(uid: String, wrapper: RemoteHistoryGroupWrapper) {
        val user = userRemoteGroup.get(uid)
        if (user != null) {
            val newUser = user.copy(
                history = user.history.map(
                    wrapper.type,
                    wrapper.historyIdGroup.toHashSet()
                )
            )
            userRemoteGroup[uid] = newUser
        }
    }

    override suspend fun getProfilePublicWithMail(hashMail: String): RemoteProfilePublic? {
        return userRemoteGroup.toList().firstOrNull{it.second.profile.hashMail == hashMail}?.second?.profile?.toProfilePublic()
    }
}