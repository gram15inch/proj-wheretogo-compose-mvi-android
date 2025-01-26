package com.dhkim139.wheretogo.mock

import com.dhkim139.wheretogo.mock.model.MockRemoteUser
import com.wheretogo.data.datasource.AuthRemoteDatasource
import com.wheretogo.domain.model.user.AuthResponse
import javax.inject.Inject

class MockAuthRemoteDatasourceImpl @Inject constructor(
    private val mockRemoteUser: MockRemoteUser
) : AuthRemoteDatasource {
    override suspend fun authOnDevice(): AuthResponse {
        return if (mockRemoteUser.token.isNotBlank()) {
            AuthResponse(
                isSuccess = true,
                data = AuthResponse.AuthData(
                    uid = mockRemoteUser.profile.uid,
                    email = mockRemoteUser.profile.private.mail,
                    userName = mockRemoteUser.profile.public.name
                )
            )
        } else {
            AuthResponse(
                isSuccess = false,
                data = null
            )
        }
    }

    override suspend fun signOutOnFirebase() {

    }

    override suspend fun deleteUser(): Boolean {
        return true
    }
}