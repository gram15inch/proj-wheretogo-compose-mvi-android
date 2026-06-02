package com.wheretogo.domain.usecase.util

interface GetImageUseCase {
    suspend operator fun invoke(imageId: String): String?
}