package com.wheretogo.domain.usecase.user

interface DeleteUserUseCase {
    suspend operator fun invoke(): Result<Unit>
}