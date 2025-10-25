package com.wheretogo.data.datasource

import com.wheretogo.domain.model.auth.SignToken
import com.wheretogo.domain.model.auth.SyncProfile

interface AuthRemoteDatasource {

    suspend fun authGoogleWithFirebase(signToken: SignToken): Result<SyncProfile>

    suspend fun signOutOnFirebase(): Result<Unit>

    suspend fun getApiToken(isForceRefresh: Boolean): Result<String?>
}