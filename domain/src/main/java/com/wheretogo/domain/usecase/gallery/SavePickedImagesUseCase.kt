package com.wheretogo.domain.usecase.gallery

import com.wheretogo.domain.model.util.MediaImage

interface SavePickedImagesUseCase {
    suspend operator fun invoke(images: List<MediaImage>): Result<List<Long>>
}