package com.wheretogo.data.datasourceimpl

import com.wheretogo.data.DataError
import com.wheretogo.data.datasource.GuestRemoteDatasource
import com.wheretogo.data.datasourceimpl.service.GuestApiService
import com.wheretogo.data.feature.dataErrorCatching
import com.wheretogo.data.feature.mapSuccess
import com.wheretogo.data.model.auth.DataSyncToken
import com.wheretogo.data.model.user.RemoteSyncUser
import com.wheretogo.data.toDataError
import javax.inject.Inject

class GuestRemoteDatasourceImpl @Inject constructor(
    private val guestApiService: GuestApiService
) : GuestRemoteDatasource {

    override suspend fun syncUser(syncToken: DataSyncToken): Result<RemoteSyncUser> {
        return dataErrorCatching {
            guestApiService.syncUser(syncToken)
        }.mapSuccess {
            when {
                !it.isSuccessful -> Result.failure(it.toDataError())
                it.body()?.data == null -> Result.failure(DataError.NotFound("empty private profile"))
                else -> Result.success(it.body()!!.data)
            }
        }
    }
}