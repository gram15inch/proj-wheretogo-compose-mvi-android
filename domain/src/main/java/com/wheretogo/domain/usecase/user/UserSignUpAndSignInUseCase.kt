package com.wheretogo.domain.usecase.user

import com.wheretogo.domain.model.UseCaseResponse
import com.wheretogo.domain.model.user.SignInRequest
import com.wheretogo.domain.model.user.SignUpRequest

interface UserSignUpAndSignInUseCase {
    suspend operator fun invoke(
        signUpRequest: SignUpRequest,
        signInRequest: SignInRequest
    ): UseCaseResponse

    suspend fun signInPass()
}