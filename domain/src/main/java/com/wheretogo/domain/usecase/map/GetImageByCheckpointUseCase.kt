package com.wheretogo.domain.usecase.map

interface GetImageByCheckpointUseCase {
    suspend operator fun invoke(fileName: String): String
}