package com.wheretogo.data.datasource

import com.wheretogo.data.model.auth.DataSyncToken
import com.wheretogo.data.model.user.RemoteSyncUser

interface GuestRemoteDatasource {
    suspend fun syncUser(syncToken: DataSyncToken): Result<RemoteSyncUser>

}