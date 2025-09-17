package com.wheretogo.data.datasource

import com.wheretogo.domain.ImageSize
import java.io.File


interface ImageLocalDatasource {

    suspend fun getImage(imageId: String, size: ImageSize): File

    suspend fun removeImage(imageId: String, size: ImageSize): Result<Unit>

    suspend fun saveImage(byteArray: ByteArray, imageId: String, size: ImageSize): Result<File>

    suspend fun openAndResizeImage(
        sourceUriString: String,
        sizeGroup: List<ImageSize>,
        compressionQuality: Int = 80
    ): Result<List<Pair<ImageSize, ByteArray>>>
}