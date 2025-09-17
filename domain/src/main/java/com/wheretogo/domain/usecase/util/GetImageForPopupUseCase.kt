package com.wheretogo.domain.usecase.util

interface GetImageForPopupUseCase {
    suspend operator fun invoke(imageId: String): String?
}