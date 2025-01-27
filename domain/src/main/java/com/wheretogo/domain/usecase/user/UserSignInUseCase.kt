package com.wheretogo.domain.usecase.user

import com.wheretogo.domain.model.UseCaseResponse

interface UserSignInUseCase {
    suspend operator fun invoke(): UseCaseResponse<String>

    suspend fun signInPass()
}