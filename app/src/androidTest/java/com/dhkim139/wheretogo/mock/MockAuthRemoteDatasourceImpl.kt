package com.dhkim139.wheretogo.mock

import com.dhkim139.wheretogo.mock.model.MockRemoteUser
import com.wheretogo.data.datasource.AuthRemoteDatasource
import com.wheretogo.domain.AuthCompany
import com.wheretogo.domain.model.auth.AuthToken
import com.wheretogo.domain.model.user.AuthProfile
import javax.inject.Inject

class MockAuthRemoteDatasourceImpl @Inject constructor(
    private val mockRemoteUser: MockRemoteUser
) : AuthRemoteDatasource {
    override suspend fun authGoogleWithFirebase(authToken: AuthToken): AuthProfile? {
        return if (mockRemoteUser.token.isNotBlank()) {
            AuthProfile(
                uid = mockRemoteUser.profile.uid,
                email = mockRemoteUser.profile.private.mail,
                userName = mockRemoteUser.profile.name,
                authCompany = AuthCompany.GOOGLE,
                token = ""
            )

        } else {
            null
        }
    }

    override suspend fun signOutOnFirebase() {

    }

    override suspend fun deleteUser(): Boolean {
        return true
    }
}