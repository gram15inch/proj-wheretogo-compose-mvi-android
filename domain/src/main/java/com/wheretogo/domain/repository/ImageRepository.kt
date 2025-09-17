package com.wheretogo.domain.repository

import com.wheretogo.domain.ImageSize
import com.wheretogo.domain.model.util.Image

interface ImageRepository {
    suspend fun getImage(imageId: String, size: ImageSize): Result<String>
    suspend fun setImage(imgUriString: String): Result<Image>
    suspend fun removeImage(imageId: String): Result<Unit>
}