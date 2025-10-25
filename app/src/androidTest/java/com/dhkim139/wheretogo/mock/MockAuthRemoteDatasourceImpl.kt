package com.dhkim139.wheretogo.mock

import com.dhkim139.wheretogo.mock.model.MockRemoteUser
import com.wheretogo.data.datasource.AuthRemoteDatasource
import com.wheretogo.domain.AuthCompany
import com.wheretogo.domain.model.auth.SignToken
import com.wheretogo.domain.model.auth.SignProfile
import com.wheretogo.domain.model.auth.SyncProfile
import com.wheretogo.presentation.AppError
import javax.inject.Inject

class MockAuthRemoteDatasourceImpl @Inject constructor(
    private val mockRemoteUser: MockRemoteUser
) : AuthRemoteDatasource {
    override suspend fun authGoogleWithFirebase(signToken: SignToken): Result<SyncProfile> {
        return if (mockRemoteUser.token.isNotBlank()) {
            Result.success(
                SyncProfile(
                    uid = mockRemoteUser.profile.uid,
                    mail = mockRemoteUser.profile.private.mail,
                    name = mockRemoteUser.profile.name,
                    authCompany = AuthCompany.GOOGLE,
                    token = ""
                )
            )


        } else {
            Result.failure(AppError.CredentialError())
        }
    }

    override suspend fun signOutOnFirebase(): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun getApiToken(isForceRefresh: Boolean): Result<String?> {
        return Result.success("")
    }
}