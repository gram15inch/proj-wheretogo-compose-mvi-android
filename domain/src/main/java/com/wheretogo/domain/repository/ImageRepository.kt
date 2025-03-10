package com.wheretogo.domain.repository

import android.net.Uri
import com.wheretogo.domain.ImageSize
import java.io.File

interface ImageRepository {
    suspend fun getImage(fileName: String, size: ImageSize): Result<File>
    suspend fun setImage(imgUri: Uri, customName: String = ""): Result<String>
    suspend fun removeImage(fileName: String): Result<Unit>
}