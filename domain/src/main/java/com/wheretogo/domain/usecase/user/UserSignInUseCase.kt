package com.wheretogo.domain.usecase.user

import com.wheretogo.domain.model.UseCaseResponse
import com.wheretogo.domain.model.user.AuthData

interface UserSignInUseCase {
    suspend operator fun invoke(authData: AuthData? = null): UseCaseResponse<String>

    suspend fun signInPass()
}