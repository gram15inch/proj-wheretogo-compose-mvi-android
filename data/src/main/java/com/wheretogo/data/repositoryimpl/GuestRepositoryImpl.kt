package com.wheretogo.data.repositoryimpl

import com.wheretogo.data.datasource.GuestRemoteDatasource
import com.wheretogo.data.toDataSyncToken
import com.wheretogo.data.toDomainResult
import com.wheretogo.data.toProfileHistoryPair
import com.wheretogo.domain.model.auth.SyncToken
import com.wheretogo.domain.model.user.History
import com.wheretogo.domain.model.user.Profile
import com.wheretogo.domain.repository.GuestRepository
import javax.inject.Inject

class GuestRepositoryImpl @Inject constructor(
    private val appRemoteDatasource: GuestRemoteDatasource
) : GuestRepository {
    override suspend fun syncUser(syncToken: SyncToken): Result<Pair<Profile, History>> {
        return appRemoteDatasource.syncUser(syncToken.toDataSyncToken())
            .mapCatching {
                it.toProfileHistoryPair()
            }.toDomainResult()
    }
}