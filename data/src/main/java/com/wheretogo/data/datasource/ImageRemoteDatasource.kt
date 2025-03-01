package com.wheretogo.data.datasource

import com.wheretogo.domain.ImageSize


interface ImageRemoteDatasource {

    suspend fun uploadImage(
        imageByteArray: ByteArray,
        imageName: String,
        size: ImageSize
    )

    suspend fun downloadImage(filename: String, size: ImageSize): ByteArray

    suspend fun removeImage(filename: String, size: ImageSize)
}