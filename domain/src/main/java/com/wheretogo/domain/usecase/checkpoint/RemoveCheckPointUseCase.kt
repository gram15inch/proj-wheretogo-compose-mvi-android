package com.wheretogo.domain.usecase.checkpoint

interface RemoveCheckPointUseCase {
    suspend operator fun invoke(checkPointId: String): Result<String>
    suspend fun bySelect(): Result<String>
    suspend fun unStamp(checkPointId: String): Result<String>
}