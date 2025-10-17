package com.dhkim139.wheretogo.mock

import com.dhkim139.wheretogo.mock.model.MockRemoteUser
import com.wheretogo.data.DataError
import com.wheretogo.data.datasource.UserRemoteDatasource
import com.wheretogo.data.feature.dataErrorCatching
import com.wheretogo.data.model.history.RemoteHistoryGroupWrapper
import com.wheretogo.data.model.user.RemoteProfilePrivate
import com.wheretogo.data.model.user.RemoteProfilePublic
import com.wheretogo.data.remoteGroupWrapper
import com.wheretogo.data.toLocalHistoryIdGroup
import com.wheretogo.data.toProfile
import com.wheretogo.data.toProfilePrivate
import com.wheretogo.data.toProfilePublic
import com.wheretogo.data.toRemoteProfilePrivate
import com.wheretogo.domain.HistoryType
import com.wheretogo.domain.get
import com.wheretogo.domain.map
import javax.inject.Inject

class MockUserRemoteDatasourceImpl @Inject constructor(
    mockRemoteUser: MockRemoteUser
) : UserRemoteDatasource {
    private var userRemoteGroup =
        mutableMapOf<String, MockRemoteUser>(mockRemoteUser.profile.uid to mockRemoteUser) // userId

    override suspend fun setProfilePublic(public: RemoteProfilePublic): Result<Unit> {
        return dataErrorCatching {
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
        }.mapCatching { Unit }
    }

    override suspend fun setProfilePrivate(
        uid: String,
        privateProfile: RemoteProfilePrivate
    ): Result<Unit> {
        return dataErrorCatching {
            val newUser = userRemoteGroup.getOrPut(uid) {
                MockRemoteUser(
                    uid,
                    getProfilePublic(uid).getOrNull()?.toProfile()
                        ?.copy(uid = uid, private = privateProfile.toProfilePrivate())
                        ?: return Result.failure(
                            DataError.UserInvalid()
                        )
                )
            }.run {
                copy(profile = profile.copy(private = privateProfile.toProfilePrivate()))
            }
            userRemoteGroup.put(uid, newUser)
        }.mapCatching { Unit }
    }

    override suspend fun getProfilePublic(uid: String): Result<RemoteProfilePublic> {
        return dataErrorCatching { userRemoteGroup.get(uid)?.profile?.toProfilePublic() }
    }

    override suspend fun getProfilePrivate(uid: String): Result<RemoteProfilePrivate> {
        return dataErrorCatching { userRemoteGroup.get(uid)?.profile?.private?.toRemoteProfilePrivate() }
    }

    override suspend fun deleteProfile(uid: String): Result<Unit> {
        return dataErrorCatching { userRemoteGroup.remove(uid) }
            .mapCatching { Unit }
    }

    override suspend fun deleteUser(userId: String): Result<String> {
        userRemoteGroup.remove(userId)
        return Result.success("")
    }

    override suspend fun addHistory(
        type: HistoryType,
        uid: String,
        groupId: String,
        historyId: String
    ): Result<Long> {
        return dataErrorCatching {
            val user = userRemoteGroup.get(uid)
            if (user != null) {
                val old = user.history.get(type).groupById.toMutableMap()
                old[groupId] = (old[groupId]?:hashSetOf()).apply {
                    add(historyId)
                }
                val newHistory = user.history.map(type, old)
                val newUser = user.copy(history = newHistory)
                userRemoteGroup[uid] = newUser
            }
            System.currentTimeMillis()
        }
    }

    override suspend fun removeHistory(
        type: HistoryType,
        uid: String,
        groupId: String,
        historyId: String
    ): Result<Unit> {
        return dataErrorCatching {
            val user = userRemoteGroup.get(uid)
            if (user != null) {
                val old = user.history.get(type).groupById.toMutableMap()
                old[groupId] = (old[groupId]?:hashSetOf()).apply {
                    remove(historyId)
                }
                val newHistory = user.history.map(type, old)
                val newUser = user.copy(history = newHistory)
                userRemoteGroup[uid] = newUser
            }
        }
    }

    override suspend fun getHistoryGroup(uid: String): Result<List<RemoteHistoryGroupWrapper>> {
        return dataErrorCatching {
            val user = userRemoteGroup[uid]
            if (user == null)
                return Result.success(emptyList())

            user.history.remoteGroupWrapper()
        }
    }

    override suspend fun setHistoryGroup(
        uid: String,
        wrapper: RemoteHistoryGroupWrapper
    ): Result<Long> {
        return dataErrorCatching {
            val user = userRemoteGroup.get(uid)
            if (user != null) {
                val newUser = user.copy(
                    history = user.history.map(
                        wrapper.type,
                        wrapper.historyIdGroup.toLocalHistoryIdGroup().groupById
                    )
                )
                userRemoteGroup[uid] = newUser
            }
            0L
        }
    }

    override suspend fun getProfilePublicWithMail(hashMail: String): Result<RemoteProfilePublic> {
        return dataErrorCatching {
            userRemoteGroup.toList()
                .firstOrNull { it.second.profile.hashMail == hashMail }?.second?.profile?.toProfilePublic()
        }
    }

    override suspend fun resisterUser(public: RemoteProfilePublic): Result<RemoteProfilePrivate> {
        return Result.success(RemoteProfilePrivate()) // todo 수정
    }
}