package com.wheretogo.domain.usecase.map

interface GetImageForPopupUseCase {
    suspend operator fun invoke(fileName: String): String
}