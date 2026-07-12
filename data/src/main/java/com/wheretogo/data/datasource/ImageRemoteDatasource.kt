package com.wheretogo.data.datasource

import com.wheretogo.data.model.gallery.ImageMetaCreateContent
import com.wheretogo.data.model.gallery.ImageMetaResponse
import com.wheretogo.domain.ImageSize
import java.io.File


interface ImageRemoteDatasource {

    suspend fun getImageMetasByUserId(userId: String): Result<List<ImageMetaResponse>>

    suspend fun setImageMetas(metas: List<ImageMetaCreateContent>): Result<List<String>>

    suspend fun removeImageMetas(metas: List<String>): Result<List<String>>

    suspend fun uploadImageByByteArray(
        imageByteArray: ByteArray,
        imageId: String,
        size: ImageSize
    ): Result<Unit>

    suspend fun uploadImageByFile(
        imageFile: File,
        imageId: String,
        size: ImageSize
    ): Result<Unit>

    suspend fun downloadImage(filename: String, size: ImageSize): Result<ByteArray>

    suspend fun removeImage(filename: String, size: ImageSize): Result<Unit>
}