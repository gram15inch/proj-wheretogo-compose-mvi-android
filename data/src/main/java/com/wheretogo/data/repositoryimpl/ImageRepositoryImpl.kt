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
import com.wheretogo.domain.model.util.ImageUris
import com.wheretogo.domain.model.util.MediaImage
import com.wheretogo.domain.repository.ImageRepository
import com.wheretogo.domain.model.util.ExifData
import com.wheretogo.domain.model.util.FilePreview
import com.wheretogo.domain.model.gallery.GalleryPhoto
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
    private fun generateId():String = "IM${ULID().nextULID()}"

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
            fetchImageOrDefault(imageId,size)
        }.mapSuccess { bytes->
            imageLocalDatasource.saveImage(bytes, imageId, size).map { file-> file.path }
        }
    }

    override suspend fun setImage(imgUriString: String): Result<ImageUris> {
        val imageId = generateId()
        return runCatching {
            val resizedImages =
                imageLocalDatasource.openAndResizeImage(imgUriString, ImageSize.entries)
                    .getOrThrow()

            val imagePaths = coroutineScope {
                resizedImages.images.map { (size, bytes) ->
                    async { uploadAndSaveImage(imageId, size, bytes) }
                }.awaitAll()
            }

            ImageUris(imageId, imagePaths.toMap())
        }.onFailure {
            coroutineScope { launch { removeImage(imageId) } }
        }.mapDataError().mapDomainError()
    }

    override suspend fun loadGalleyPhotos(): Result<List<GalleryPhoto>> {
        return imageLocalDatasource.loadGalleyPhotos()
    }

    override suspend fun saveGalleryPhotos(imgUriStrings: List<String>): Result<List<Long>> {
        return imageLocalDatasource.saveGalleryPhotos(imgUriStrings)
    }

    override suspend fun clearGalleryPhotos(ids: Set<Long>): Result<Set<Long>> {
        return imageLocalDatasource.clearGalleryPhotos(ids)
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

    override suspend fun getExif(imageUriString: String): Result<ExifData> {
        return imageLocalDatasource.getExif(imageUriString)
    }

    override suspend fun getPreview(imageUriString: String): Result<FilePreview> {
        return imageLocalDatasource.getPreview(imageUriString)
    }

    override suspend fun getMediaImages(
        offset: Int,
        limit: Int
    ): Result<List<MediaImage>> {
        return imageLocalDatasource.getMediaImages(offset, limit)
    }

    private suspend fun uploadAndSaveImage(imageId: String, size: ImageSize, bytes: ByteArray): Pair<ImageSize, String> {
        imageRemoteDatasource.uploadImage(bytes, imageId, size).getOrThrow()
        return imageLocalDatasource.saveImage(bytes, imageId, size).getOrThrow()
            .run {
               val path= toUri().path ?: throw DataError.InternalError("이미지 저장 실패: $size")
               size to path
            }
    }


    private suspend fun fetchImageOrDefault(imageId: String, size: ImageSize): Result<ByteArray> {
        return imageRemoteDatasource.downloadImage(imageId, size).fold(
            onSuccess = { Result.success(it) },
            onFailure = {
                imageLocalDatasource.getDefaultImageBytes(size)
            }
        )
    }
}