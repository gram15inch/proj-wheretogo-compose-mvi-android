package com.wheretogo.domain.usecase.app

interface AppCheckBySignatureUseCase {
    suspend operator fun invoke(): Result<Boolean>
}