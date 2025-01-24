package com.dhkim139.wheretogo.mock

import android.net.Uri
import com.wheretogo.data.datasource.ImageRemoteDatasource
import com.wheretogo.domain.ImageSize
import java.io.File
import javax.inject.Inject

class MockImageRemoteDatasourceImpl @Inject constructor() : ImageRemoteDatasource {

    override suspend fun setImage(uri: Uri, filename: String, size: ImageSize): Boolean {
        return true
    }

    override suspend fun getImage(localFile: File, filename: String, size: ImageSize): File? {
        return localFile
    }

    override suspend fun removeImage(filename: String, size: ImageSize): Boolean{
        return true
    }
}