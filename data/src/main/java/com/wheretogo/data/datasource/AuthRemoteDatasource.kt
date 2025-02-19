package com.wheretogo.data.datasource

import com.wheretogo.domain.model.auth.AuthToken
import com.wheretogo.domain.model.user.AuthResponse

interface AuthRemoteDatasource {

    suspend fun authGoogleWithFirebase(authToken: AuthToken): AuthResponse

    suspend fun signOutOnFirebase()

    suspend fun deleteUser(): Boolean
}