package com.wheretogo.data.datasource

import com.wheretogo.domain.model.user.AuthResponse

interface AuthRemoteDatasource {

    suspend fun authOnDevice(): AuthResponse

    suspend fun signOutOnFirebase()

    suspend fun deleteUser(): Boolean
}