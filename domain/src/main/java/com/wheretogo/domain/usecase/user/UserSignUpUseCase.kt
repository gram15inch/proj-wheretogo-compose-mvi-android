package com.wheretogo.domain.usecase.user

import com.wheretogo.domain.model.UseCaseResponse

interface UserSignUpUseCase {
    suspend operator fun invoke(): UseCaseResponse
}