package com.wheretogo.data.repositoryimpl

import android.net.Uri
import androidx.core.net.toUri
import com.wheretogo.data.datasource.ImageLocalDatasource
import com.wheretogo.data.datasource.ImageRemoteDatasource
import com.wheretogo.domain.ImageSize
import com.wheretogo.domain.repository.ImageRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.io.File
import javax.inject.Inject

class ImageRepositoryImpl @Inject constructor(
    private val imageRemoteDatasource: ImageRemoteDatasource,
    private val imageLocalDatasource: ImageLocalDatasource
) : ImageRepository {
    override suspend fun getImage(fileName: String, size: ImageSize): File? {
        return imageLocalDatasource.getImage(fileName, size).run {
            if (this.exists())
                this
            else {
                imageRemoteDatasource.getImage(this, fileName, size)
            }
        }
    }

    override suspend fun setImage(imgUri: Uri, fileName: String): Boolean {
        return coroutineScope {
            imageLocalDatasource.setImage(imgUri, fileName)
            ImageSize.entries.map { size ->
                async {
                    val localFile = imageLocalDatasource.getImage(fileName, size)
                    imageRemoteDatasource.setImage(
                        localFile.toUri(), fileName, size
                    )
                }
            }.awaitAll().all { it }
        }
    }

    override suspend fun removeImage(fileName: String): Boolean {
        return coroutineScope {
            ImageSize.entries.map { size ->
                async {
                    imageRemoteDatasource.removeImage(fileName, size).apply {
                        imageLocalDatasource.removeImage(fileName, size)
                    }
                }
            }.awaitAll().all { it }
        }
    }
}