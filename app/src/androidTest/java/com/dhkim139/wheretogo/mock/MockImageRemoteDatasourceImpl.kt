package com.dhkim139.wheretogo.mock

import com.wheretogo.data.datasource.ImageRemoteDatasource
import com.wheretogo.domain.ImageSize
import javax.inject.Inject

class MockImageRemoteDatasourceImpl @Inject constructor() : ImageRemoteDatasource {

    override suspend fun uploadImage(
        imageByteArray: ByteArray,
        imageName: String,
        size: ImageSize
    ) {

    }

    override suspend fun downloadImage(filename: String, size: ImageSize): ByteArray {
        return byteArrayOf()
    }

    override suspend fun removeImage(filename: String, size: ImageSize) {

    }
}