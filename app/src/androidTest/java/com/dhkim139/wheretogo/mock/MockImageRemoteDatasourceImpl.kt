package com.dhkim139.wheretogo.mock

import com.wheretogo.data.datasource.ImageRemoteDatasource
import com.wheretogo.domain.ImageSize
import javax.inject.Inject

class MockImageRemoteDatasourceImpl @Inject constructor() : ImageRemoteDatasource {

    override suspend fun uploadImage(
        imageByteArray: ByteArray,
        imageId: String,
        size: ImageSize
    ): Result<Unit> {
        return runCatching { }
    }

    override suspend fun downloadImage(filename: String, size: ImageSize): Result<ByteArray> {
        return runCatching { byteArrayOf() }
    }

    override suspend fun removeImage(filename: String, size: ImageSize): Result<Unit> {
        return runCatching { }
    }
}