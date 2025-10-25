package com.wheretogo.data.datasource

import com.wheretogo.data.model.user.RemoteSyncUser
import com.wheretogo.domain.model.auth.SyncToken

interface GuestRemoteDatasource {
    suspend fun syncUser(syncToken: SyncToken): Result<RemoteSyncUser>

}