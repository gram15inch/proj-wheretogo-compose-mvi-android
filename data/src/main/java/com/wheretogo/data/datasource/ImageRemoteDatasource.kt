package com.wheretogo.data.datasource

import android.net.Uri
import com.wheretogo.domain.ImageSize
import java.io.File


interface ImageRemoteDatasource {

    suspend fun setImage(uri: Uri, filename: String, size: ImageSize): Boolean

    suspend fun getImage(localFile: File, filename: String, size: ImageSize): File?

    suspend fun removeImage(filename: String, size: ImageSize): Boolean
}