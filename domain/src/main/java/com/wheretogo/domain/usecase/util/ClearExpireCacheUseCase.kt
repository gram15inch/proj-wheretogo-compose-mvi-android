package com.wheretogo.domain.usecase.util

interface ClearExpireCacheUseCase {
    suspend operator fun invoke(): Result<Int>
}