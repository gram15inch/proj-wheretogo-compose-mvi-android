package com.wheretogo.data.datasource

import com.wheretogo.domain.model.auth.AuthToken
import com.wheretogo.domain.model.user.AuthProfile

interface AuthRemoteDatasource {

    suspend fun authGoogleWithFirebase(authToken: AuthToken): Result<AuthProfile>

    suspend fun signOutOnFirebase(): Result<Unit>

    suspend fun getApiToken(isForceRefresh: Boolean): Result<String?>
}