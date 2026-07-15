package com.wheretogo.data.repositoryimpl

import androidx.core.net.toUri
import com.wheretogo.data.DataError
import com.wheretogo.data.datasource.ImageLocalDatasource
import com.wheretogo.data.datasource.ImageRemoteDatasource
import com.wheretogo.data.feature.mapDataError
import com.wheretogo.data.feature.mapDomainError
import com.wheretogo.data.feature.mapSuccess
import com.wheretogo.data.model.gallery.PhotoEntity
import com.wheretogo.data.toCreateContent
import com.wheretogo.data.toDomain
import com.wheretogo.data.toGalleryPhoto
import com.wheretogo.data.toPhotoEntity
import com.wheretogo.domain.ImageSize
import com.wheretogo.domain.feature.flatMap
import com.wheretogo.domain.model.gallery.GalleryPhoto
import com.wheretogo.domain.model.util.ExifData
import com.wheretogo.domain.model.util.FilePreview
import com.wheretogo.domain.model.util.ImageUris
import com.wheretogo.domain.model.util.MediaImage
import com.wheretogo.domain.repository.ImageRepository
import de.huxhorn.sulky.ulid.ULID
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
            val encodedImages =
                imageLocalDatasource.encodeImage(imgUriString, ImageSize.entries)
                    .getOrThrow()

            val imagePaths = coroutineScope {
                encodedImages.images.map { (size, bytes) ->
                    async { uploadAndSaveImage(imageId, size, bytes) }
                }.awaitAll()
            }

            ImageUris(imageId, imagePaths.toMap())
        }.onFailure {
            coroutineScope { launch { removeImage(imageId) } }
        }.mapDataError().mapDomainError()
    }


    override suspend fun setRemoteImage(imageId: String): Result<ImageUris> {
        return runCatching {
            coroutineScope {
                val imagePaths=  ImageSize.entries.map { size->
                    async {
                        val file = imageLocalDatasource.getImage(imageId,size)
                        if(!file.exists())
                            throw DataError.NotFound("$imageId not found")
                        imageRemoteDatasource.uploadImageByFile(file, imageId, size)
                        size to file.toUri().toString()
                    }
                }.awaitAll()
                ImageUris(imageId, imagePaths.toMap())
            }
        }.onFailure {
            coroutineScope { launch { removeImage(imageId) } }
        }.mapDataError().mapDomainError()
    }

    override suspend fun setRemoteImageWithMeta(imageId: String, userId:String): Result<ImageUris> {
        return runCatching {
            coroutineScope {
                val metaDeprecated= async {
                    val entity= imageLocalDatasource.getPhotosByImageId(listOf(imageId)).getOrThrow().firstOrNull()
                    if(entity== null || entity.exif == null)
                        throw DataError.NotFound("$imageId not found")
                    val content = entity.toCreateContent().copy(userId = userId)
                    imageRemoteDatasource.setImageMetas(listOf(content))
                }
                val imagePaths=  ImageSize.entries.map { size->
                    async {
                        val file = imageLocalDatasource.getImage(imageId,size)
                        if(!file.exists())
                            throw DataError.NotFound("$imageId not found")
                        imageRemoteDatasource.uploadImageByFile(file, imageId, size)
                        size to file.toUri().toString()
                    }
                }.awaitAll()
                metaDeprecated.await()
                ImageUris(imageId, imagePaths.toMap())
            }
        }.onFailure {
            coroutineScope { launch { removeImageWithMeta(imageId) } }
        }.mapDataError().mapDomainError()
    }

    override suspend fun removeRemoteImage(imageId: String): Result<Unit> {
        return runCatching {
            coroutineScope {
                ImageSize.entries.map { size ->
                    async {
                        imageRemoteDatasource.removeImage(imageId, size)
                    }
                }.awaitAll().flatMap()
            }
        }
    }

    override suspend fun getGalleryPhotosByImageId(imageIds: List<String>): Result<List<GalleryPhoto>> {
        return imageLocalDatasource.getPhotosByImageId(imageIds).map { entities ->
            entities.mapNotNull { it.toGalleryPhoto() }
        }
    }

    override fun observeGalleryPhotos(): Flow<List<GalleryPhoto>> {
        return imageLocalDatasource.observePhotos().map { entities ->
            entities.mapNotNull { it.toGalleryPhoto() }
        }
    }

    override suspend fun refreshGalleyPhotosByUserId(userId: String): Result<Unit> {
        return runCatching {
            coroutineScope {
                val response= imageRemoteDatasource.getImageMetasByUserId(userId).getOrThrow()
                val entity= imageLocalDatasource.getPhotosByHash(response.map { it.sha256 }).getOrThrow()
                val merge=
                    response.map { res->
                        val local = entity.firstOrNull{ it.sha256 == res.sha256 }
                        async {
                            if(local == null){
                                val small = getImage(res.imageId, ImageSize.SMALL).getOrThrow().toUri().toString()
                                val normal = getImage(res.imageId, ImageSize.NORMAL).getOrThrow().toUri().toString()
                                res.toPhotoEntity().copy(
                                    imageId = res.imageId,
                                    thumbnail = small,
                                    uriString = normal,
                                )
                            } else {
                                local.copy(imageId = res.imageId, stampAt = res.createAt)
                            }
                        }
                    }.awaitAll()
                imageLocalDatasource.upsertPhotos(merge)
            }

        }
    }

    override suspend fun saveGalleryPhotos(imgUriStrings: List<String>): Result<List<Long>> {
        return imageLocalDatasource.saveGalleryPhotos(imgUriStrings)
    }

    override suspend fun updateGalleryPhotos(photos: List<GalleryPhoto>): Result<Unit> {
        val entities= photos.map {
            PhotoEntity(
                id = it.entityId,
                sha256 = it.sha256,
                imageId = it.imageId,
                courseId = it.courseId,
                courseName = it.courseName,
                stampAt = it.stampAt,
            )
        }
        return imageLocalDatasource.updatePhotos(entities)
    }

    override suspend fun clearStampAt(imageIds: List<String>): Result<Unit> {
        return imageLocalDatasource.clearStampAt(imageIds)
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

    override suspend fun removeLocalImage(imageId: String): Result<Unit> {
        return coroutineScope {
            ImageSize.entries.map { size ->
                async {
                    imageLocalDatasource.removeImage(imageId, size)
                }
            }.awaitAll().flatMap()
        }.mapCatching { Unit }.mapDataError().mapDomainError()
    }

    override suspend fun removeImageWithMeta(imageId: String): Result<Unit> {
        return coroutineScope {
            launch {
                imageRemoteDatasource.removeImageMetas(listOf(imageId))
            }
            ImageSize.entries.map { size ->
                async {
                    imageRemoteDatasource.removeImage(imageId, size).mapSuccess {
                        imageLocalDatasource.removeImage(imageId, size)
                    }
                }
            }.awaitAll().flatMap()
        }.mapCatching {}.mapDataError().mapDomainError()
    }

    override suspend fun getExif(imageUriString: String): Result<ExifData> {
        return imageLocalDatasource.getExif(imageUriString).map { it.toDomain() }
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
        imageRemoteDatasource.uploadImageByByteArray(bytes, imageId, size).getOrThrow()
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