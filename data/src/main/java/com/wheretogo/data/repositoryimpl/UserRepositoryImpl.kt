package com.wheretogo.data.repositoryimpl

import com.wheretogo.data.DataError
import com.wheretogo.data.datasource.UserLocalDatasource
import com.wheretogo.data.datasource.UserRemoteDatasource
import com.wheretogo.data.feature.dataErrorCatching
import com.wheretogo.data.feature.mapDomainError
import com.wheretogo.data.feature.mapSuccess
import com.wheretogo.data.toHistoryGroupWrapper
import com.wheretogo.data.toLocalHistory
import com.wheretogo.data.toLocalProfile
import com.wheretogo.data.toProfile
import com.wheretogo.data.toProfilePrivate
import com.wheretogo.data.toProfilePublic
import com.wheretogo.data.toRemoteProfilePrivate
import com.wheretogo.domain.HistoryType
import com.wheretogo.domain.feature.domainFlatMap
import com.wheretogo.domain.feature.hashSha256
import com.wheretogo.domain.feature.zip
import com.wheretogo.domain.model.history.HistoryGroupWrapper
import com.wheretogo.domain.model.user.AuthProfile
import com.wheretogo.domain.model.user.Profile
import com.wheretogo.domain.repository.UserRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userLocalDatasource: UserLocalDatasource,
    private val userRemoteDatasource: UserRemoteDatasource
) : UserRepository {

    override suspend fun getProfileStream(): Flow<Profile> {
        return userLocalDatasource.getProfileFlow().map { it.toProfile() }
    }

    override suspend fun createUser(profile: Profile): Result<Unit> {
        return runCatching {
            val userId = profile.uid
            userId.isBlank().let { if (it) throw DataError.UserInvalid() }
        }.mapSuccess {
            val remotePublic = profile.toProfilePublic()
            val remotePrivate = profile.private.toRemoteProfilePrivate()
            val local = remotePublic.toLocalProfile(remotePrivate)
            userRemoteDatasource.setProfilePublic(remotePublic).zip(
                userRemoteDatasource.setProfilePrivate(
                    profile.uid,
                    remotePrivate
                )
            ) { _, _ -> userLocalDatasource.setProfile(local) }
                .mapSuccess {
                    coroutineScope {
                        val resultGroup= HistoryType.entries.mapNotNull { type->
                            if(type in listOf(HistoryType.COURSE, HistoryType.CHECKPOINT, HistoryType.COMMENT))
                                async {
                                    userRemoteDatasource.addHistory(
                                        type = type,
                                        uid = profile.uid,
                                        groupId = "",
                                        historyId = ""
                                    ).mapSuccess { addedAt ->
                                        userLocalDatasource.addHistory(
                                            type = type,
                                            groupId = "",
                                            historyId = "",
                                            addedAt = addedAt
                                        )
                                    }
                                }
                            else null
                        }.awaitAll()
                        resultGroup.domainFlatMap {
                            Result.success(Unit)
                        }
                    }
                }
        }.mapDomainError()
    }

    override suspend fun deleteUser(): Result<Unit> {
        return runCatching {
            val uid = userLocalDatasource.getProfileFlow().first().uid
            check(uid.isNotBlank())
            uid
        }.mapSuccess { uid ->
            userRemoteDatasource.deleteUser(uid)
        }.mapCatching {
            clearCache()
        }.mapDomainError()
    }

    override suspend fun syncUser(authProfile: AuthProfile): Result<Profile> {
        return getRemoteProfile(authProfile.uid)
            .mapSuccess { profile ->
                val newProfile = profile.copy(
                    private = profile.private.copy(lastVisited = System.currentTimeMillis())
                )
                userLocalDatasource.setProfile(newProfile.toLocalProfile()).mapSuccess {
                    coroutineScope {
                       val visitUpdate= async {
                            userRemoteDatasource.setProfilePrivate(
                                newProfile.uid,
                                newProfile.private.toRemoteProfilePrivate()
                            )
                        }

                       val history= async {
                           userRemoteDatasource.getHistoryGroup(authProfile.uid)
                               .mapSuccess { history -> userLocalDatasource.iniHistory(history.toLocalHistory()) }
                       }

                        visitUpdate.await()
                        history.await()
                    }
                }.mapCatching { newProfile }
            }.mapDomainError()
    }

    override suspend fun clearCache() {
        userLocalDatasource.clearUser()
    }

    override suspend fun checkUser(mail: String): Result<Profile> {
        return runCatching {
            hashSha256(mail)
        }.mapSuccess { hashMail ->
            userRemoteDatasource.getProfilePublicWithMail(hashMail)
                .mapCatching { it.toProfile() }
        }.mapDomainError()
    }


    private suspend fun getRemoteProfile(userId: String): Result<Profile> {
        return coroutineScope {
            val public = async { userRemoteDatasource.getProfilePublic(userId) }
            val private = async { userRemoteDatasource.getProfilePrivate(userId) }

            public.await().zip(private.await(), { public, private ->
                public.toProfile().copy(
                    private = private.toProfilePrivate()
                )
            })
        }.mapDomainError()
    }


    // history
    override suspend fun getHistory(type: HistoryType): Result<HistoryGroupWrapper> {
        return dataErrorCatching {
            userLocalDatasource.getHistory(type).toHistoryGroupWrapper()
        }.mapDomainError()
    }



    override suspend fun addHistory(
        type: HistoryType,
        groupId:String,
        historyId: String
    ): Result<Unit> {
        return dataErrorCatching {
            val profile = validateUser()
            require(historyId.isNotEmpty()) { "inValid historyId: $historyId" }
            profile
        }.mapSuccess { profile ->
            userRemoteDatasource.addHistory(type, profile.uid, groupId, historyId)
        }.mapSuccess {
            userLocalDatasource.addHistory(type, groupId, historyId,it)
        }.mapDomainError()
    }

    override suspend fun addHistoryToLocal(
        type: HistoryType,
        groupId: String,
        historyId: String,
        addedAt: Long
    ): Result<Unit> {
        return dataErrorCatching {
            validateUser()
            require(historyId.isNotEmpty()) { "inValid historyId: $historyId" }
        }.mapSuccess {
            userLocalDatasource.addHistory(type, groupId, historyId, addedAt)
        }.mapDomainError()
    }

    override suspend fun removeHistory(
        type: HistoryType,
        groupId:String,
        historyId: String
    ): Result<Unit> {
        return dataErrorCatching {
            val profile = validateUser()
            require(historyId.isNotEmpty()) { "inValid historyId: $historyId" }
            profile
        }.mapSuccess { profile ->
            userLocalDatasource.removeHistory(type, groupId, historyId)
                .mapSuccess {
                    userRemoteDatasource.removeHistory(
                        type = type,
                        uid = profile.uid,
                        groupId = groupId,
                        historyId = historyId
                    )
                }
        }.mapDomainError()
    }

    override suspend fun removeHistoryToLocal(
        type: HistoryType,
        groupId: String,
        historyId: String
    ): Result<Unit> {
        return dataErrorCatching {
            validateUser()
            require(historyId.isNotEmpty()) { "inValid historyId: $historyId" }
        }.mapSuccess {
            userLocalDatasource.removeHistory(type, groupId, historyId)
        }.mapDomainError()
    }

    private suspend fun validateUser(): Profile {
        val profile = userLocalDatasource.getProfileFlow().first()

        if (profile.uid.isBlank())
            throw DataError.UserInvalid("")

        return profile.toProfile()
    }
}