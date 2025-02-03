package com.wheretogo.domain.usecase.user

import com.wheretogo.domain.model.UseCaseResponse
import com.wheretogo.domain.model.user.AuthProfile

interface UserSignInUseCase {
    suspend operator fun invoke(authProfile: AuthProfile): UseCaseResponse<String>

    suspend fun signInPass()
}