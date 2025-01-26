package com.wheretogo.domain.usecase.user

import com.wheretogo.domain.model.UseCaseResponse

interface UserSignUpAndSignInUseCase {
    suspend operator fun invoke(): UseCaseResponse
    suspend fun signInPass()
}