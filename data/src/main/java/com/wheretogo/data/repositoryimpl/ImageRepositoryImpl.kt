package com.wheretogo.data.repositoryimpl

import androidx.core.net.toUri
import com.wheretogo.data.DataError
import com.wheretogo.data.datasource.ImageLocalDatasource
import com.wheretogo.data.datasource.ImageRemoteDatasource
import com.wheretogo.data.feature.mapDataError
import com.wheretogo.data.feature.mapDomainError
import com.wheretogo.data.feature.mapSuccess
import com.wheretogo.domain.ImageSize
import com.wheretogo.domain.feature.flatMap
import com.wheretogo.domain.model.util.Image
import com.wheretogo.domain.repository.ImageRepository
import de.huxhorn.sulky.ulid.ULID
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class ImageRepositoryImpl @Inject constructor(
    private val imageRemoteDatasource: ImageRemoteDatasource,
    private val imageLocalDatasource: ImageLocalDatasource
) : ImageRepository {

    override suspend fun getImage(imageId: String, size: ImageSize): Result<String> {
        return runCatching {
            val file = imageLocalDatasource.getImage(imageId, size)
            if (file.exists()) {
                val path = file.toUri().path
                if (path.isNullOrBlank())
                    return Result.failure(DataError.InternalError("이미지 가져오기 오류"))
                return Result.success(path)
            }
        }.mapSuccess {
            imageRemoteDatasource.downloadImage(imageId, size)
        }.mapSuccess {
            imageLocalDatasource.saveImage(it, imageId, size)
                .mapCatching {
                    it.path
                }
        }
    }

    override suspend fun setImage(imgUriString: String): Result<Image> {
        val imageId = "IM${ULID().nextULID()}"

        return imageLocalDatasource.openAndResizeImage(imgUriString, ImageSize.entries)
            .mapSuccess {
                runCatching {
                    coroutineScope {
                        it.map { image ->
                            async {
                                val size = image.first
                                val byteArray = image.second
                                imageRemoteDatasource.uploadImage(byteArray, imageId, size)
                                    .mapSuccess {
                                        imageLocalDatasource.saveImage(byteArray, imageId, size)
                                            .mapCatching {
                                                it.toUri().path
                                            }
                                    }.mapSuccess { path ->
                                        if (path == null)
                                            return@mapSuccess Result.failure(
                                                DataError.InternalError(
                                                    "이미지 로컬 저장 오류"
                                                )
                                            )
                                        else
                                            Result.success(size to path)
                                    }
                            }
                        }.awaitAll()
                    }
                }
            }.mapSuccess {
                it.flatMap()
            }.mapCatching {
                Image(imageId, it.toMap())
            }.onFailure {
                coroutineScope {
                    launch { removeImage(imageId) }
                }
            }
            .mapDataError().mapDomainError()
    }

    override suspend fun removeImage(imageId: String): Result<Unit> {
        return coroutineScope {
            ImageSize.entries.map { size ->
                async {
                    imageRemoteDatasource.removeImage(imageId, size).mapSuccess {
                        imageLocalDatasource.removeImage(imageId, size)
                    }
                }
            }.awaitAll().flatMap()
        }.mapCatching { Unit }.mapDataError().mapDomainError()
    }

}