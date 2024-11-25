package com.wheretogo.domain.usecase

import com.wheretogo.domain.model.UseCaseResponse
import com.wheretogo.domain.model.user.SignInRequest

interface UserSignInUseCase {
    suspend operator fun invoke(request: SignInRequest): UseCaseResponse

    suspend fun signInPass()
}