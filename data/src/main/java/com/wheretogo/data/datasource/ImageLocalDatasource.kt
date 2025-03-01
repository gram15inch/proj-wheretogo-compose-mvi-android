package com.wheretogo.data.datasource

import android.net.Uri
import com.wheretogo.domain.ImageSize
import java.io.File


interface ImageLocalDatasource {

    suspend fun getImage(fileName: String, size: ImageSize): File

    suspend fun removeImage(fileName: String, size: ImageSize)

    suspend fun saveImage(byteArray: ByteArray, fileName: String, size: ImageSize): Uri

    suspend fun openAndResizeImage(
        sourceUri: Uri,
        sizeGroup: List<ImageSize>,
        compressionQuality: Int = 80
    ): List<Pair<ImageSize, ByteArray>>
}