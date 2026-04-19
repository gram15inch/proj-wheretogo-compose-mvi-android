package com.wheretogo.domain.repository

import com.wheretogo.domain.ImageSize
import com.wheretogo.domain.model.util.ImageUris

interface ImageRepository {
    suspend fun getImage(imageId: String, size: ImageSize): Result<String>
    suspend fun setImage(imgUriString: String): Result<ImageUris>
    suspend fun removeImage(imageId: String): Result<Unit>
}