package com.wheretogo.domain.usecase.gallery

interface DeleteGalleryPhotosUseCase {
    suspend operator fun invoke(ids: Set<Long>): Result<List<Long>>
}