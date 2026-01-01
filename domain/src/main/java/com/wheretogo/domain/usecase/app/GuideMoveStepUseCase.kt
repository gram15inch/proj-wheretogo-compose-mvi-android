package com.wheretogo.domain.usecase.app

interface GuideMoveStepUseCase {
    suspend operator fun invoke(isNext: Boolean): Result<Unit>
    suspend fun start(): Result<Unit>
    suspend fun skip(): Result<Unit>
}