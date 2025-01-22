package com.wheretogo.domain.usecase.user

import com.wheretogo.domain.model.UseCaseResponse

interface UserSignOutUseCase {
    suspend operator fun invoke(): UseCaseResponse
}