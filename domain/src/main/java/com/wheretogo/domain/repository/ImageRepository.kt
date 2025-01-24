package com.wheretogo.domain.repository

import android.net.Uri
import com.wheretogo.domain.ImageSize
import java.io.File

interface ImageRepository {
    suspend fun getImage(fileName: String, size: ImageSize): File?
    suspend fun setImage(imgUri: Uri, fileName: String): Boolean
    suspend fun removeImage(fileName: String): Boolean
}