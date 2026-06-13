package com.wheretogo.domain.usecase.util

import com.wheretogo.domain.model.util.MediaImage

interface GetImagesPageUseCase {
    suspend operator fun invoke(offset: Int, limit: Int): Result<List<MediaImage>>
}