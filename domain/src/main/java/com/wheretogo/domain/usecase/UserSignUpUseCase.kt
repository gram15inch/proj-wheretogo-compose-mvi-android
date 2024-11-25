package com.wheretogo.domain.usecase

import com.wheretogo.domain.model.UseCaseResponse
import com.wheretogo.domain.model.user.SignUpRequest

interface UserSignUpUseCase {
    suspend operator fun invoke(request: SignUpRequest): UseCaseResponse
}