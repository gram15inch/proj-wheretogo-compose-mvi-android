package com.wheretogo.domain.usecase.gallery

import com.wheretogo.domain.model.gallery.GalleryPhoto

interface LoadGalleryPhotosUseCase {
    suspend operator fun invoke(): Result<List<GalleryPhoto>>
}