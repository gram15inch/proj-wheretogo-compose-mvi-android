package com.dhkim139.wheretogo.mock

import com.dhkim139.wheretogo.mock.model.MockRemoteUser
import com.wheretogo.data.datasource.AuthRemoteDatasource
import com.wheretogo.domain.model.user.AuthResponse
import javax.inject.Inject

class MockAuthRemoteDatasourceImpl @Inject constructor(
    private val mockRemoteUser: MockRemoteUser
) : AuthRemoteDatasource {
    override suspend fun authWithGoogle(idToken: String): AuthResponse {
        return if (idToken == mockRemoteUser.token) {
            AuthResponse(
                isSuccess = true,
                data = AuthResponse.AuthData(
                    uid = mockRemoteUser.profile.uid,
                    id = mockRemoteUser.profile.private.mail
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