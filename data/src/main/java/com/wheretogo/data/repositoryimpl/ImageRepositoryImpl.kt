package com.wheretogo.data.repositoryimpl

import android.net.Uri
import com.wheretogo.data.datasource.ImageLocalDatasource
import com.wheretogo.data.datasource.ImageRemoteDatasource
import com.wheretogo.domain.ImageSize
import com.wheretogo.domain.repository.ImageRepository
import de.huxhorn.sulky.ulid.ULID
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.io.File
import javax.inject.Inject

class ImageRepositoryImpl @Inject constructor(
    private val imageRemoteDatasource: ImageRemoteDatasource,
    private val imageLocalDatasource: ImageLocalDatasource
) : ImageRepository {
    override suspend fun getImage(fileName: String, size: ImageSize): Result<File> {
        return runCatching {
            imageLocalDatasource.getImage(fileName, size).run {
                if (!exists()) {
                    val byteArray = imageRemoteDatasource.downloadImage(fileName, size)
                    imageLocalDatasource.saveImage(byteArray, fileName, size)
                }
                this
            }
        }
    }

    override suspend fun setImage(imgUri: Uri, customName: String): Result<String> {
        return runCatching {
            coroutineScope {
                val imageName = customName.ifBlank { "${ULID().nextULID()}.jpg" }
                imageLocalDatasource.openAndResizeImage(imgUri, ImageSize.entries).map { image ->
                    async {
                        val size = image.first
                        val byteArray = image.second
                        imageRemoteDatasource.uploadImage(byteArray, imageName, size)
                        imageLocalDatasource.saveImage(byteArray, imageName, size)
                    }
                }.awaitAll()
                imageName
            }
        }
    }

    override suspend fun removeImage(fileName: String): Result<Unit> {
        return runCatching {
            coroutineScope {
                ImageSize.entries.map { size ->
                    async {
                        imageRemoteDatasource.removeImage(fileName, size)
                        imageLocalDatasource.removeImage(fileName, size)
                    }
                }.awaitAll()
            }
        }
    }


}