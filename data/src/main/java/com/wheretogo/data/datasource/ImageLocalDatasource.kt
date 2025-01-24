package com.wheretogo.data.datasource

import android.net.Uri
import com.wheretogo.domain.ImageSize
import java.io.File


interface ImageLocalDatasource {

    suspend fun getImage(fileName: String, size: ImageSize): File

    suspend fun setImage(uri: Uri, fileName: String)

    suspend fun removeImage(fileName: String, size: ImageSize)

}