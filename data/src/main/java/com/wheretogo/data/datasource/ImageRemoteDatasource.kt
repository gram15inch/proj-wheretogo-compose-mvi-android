package com.wheretogo.data.datasource

import com.wheretogo.domain.ImageSize


interface ImageRemoteDatasource {

    suspend fun uploadImage(
        imageByteArray: ByteArray,
        imageId: String,
        size: ImageSize
    ): Result<Unit>

    suspend fun downloadImage(filename: String, size: ImageSize): Result<ByteArray>

    suspend fun removeImage(filename: String, size: ImageSize): Result<Unit>
}