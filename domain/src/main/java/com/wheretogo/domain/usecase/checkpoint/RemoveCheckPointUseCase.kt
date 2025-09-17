package com.wheretogo.domain.usecase.checkpoint

interface RemoveCheckPointUseCase {
    suspend operator fun invoke(courseId: String, checkPointId: String): Result<String>
}