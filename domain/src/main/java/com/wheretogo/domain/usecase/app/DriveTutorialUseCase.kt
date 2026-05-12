package com.wheretogo.domain.usecase.app

import com.wheretogo.domain.DriveTutorialStep

interface DriveTutorialUseCase {
    suspend operator fun invoke(validateStep: DriveTutorialStep, data: Any? = null): Result<Unit>
    suspend fun start(): Result<Unit>
    suspend fun skip(): Result<Unit>
}