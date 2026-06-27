package com.wheretogo.data.datasourceimpl

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import com.wheretogo.data.ImageFormat
import com.wheretogo.data.datasource.ImageLocalDatasource
import com.wheretogo.data.datasourceimpl.database.GalleryDatabase
import com.wheretogo.data.feature.PhotoExifReader
import com.wheretogo.data.feature.downSampling
import com.wheretogo.data.feature.exif
import com.wheretogo.data.feature.rotateByExif
import com.wheretogo.data.feature.scaleCropToFill
import com.wheretogo.data.feature.scaleToFitInside
import com.wheretogo.data.model.confg.ImageConfig
import com.wheretogo.data.model.gallery.EncodedImage
import com.wheretogo.data.model.gallery.PhotoEntity
import com.wheretogo.domain.ImageSize
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.gallery.GalleryPhoto
import com.wheretogo.domain.model.gallery.PhotoExif
import com.wheretogo.domain.model.util.ExifData
import com.wheretogo.domain.model.util.FilePreview
import com.wheretogo.domain.model.util.MediaImage
import dagger.hilt.android.qualifiers.ApplicationContext
import de.huxhorn.sulky.ulid.ULID
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Locale
import javax.inject.Inject

class ImageLocalDatasourceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imageFile: File,
    private val imageConfig: ImageConfig,
    private val galleryDatabase: GalleryDatabase,
    private val exifReader: PhotoExifReader
) : ImageLocalDatasource {
    private val photoDao by lazy { galleryDatabase.photoDao() }
    private val ext = imageConfig.format.ext
    private val mediaUri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

    override suspend fun getImage(imageId: String, size: ImageSize): File {
        val localFile =
            File(
                imageFile.parentFile,
                "image/${size.pathName}/${imageId}.$ext"
            ).apply {
                if (!parentFile!!.exists()) {
                    parentFile?.mkdirs()
                }
            }
        return localFile
    }

    override suspend fun saveImage(
        byteArray: ByteArray,
        imageId: String,
        size: ImageSize
    ): Result<File> {
        return runCatching {
            val file = getImage(imageId, size)
            file.outputStream().use { steam ->
                steam.write(byteArray)
            }
            file
        }
    }


    override suspend fun removeImage(imageId: String, size: ImageSize): Result<Boolean> {
        return runCatching {
            val localFile = getImage(imageId, size)
            if (localFile.exists())
                localFile.delete()
            else true
        }
    }

    override suspend fun getDefaultImageBytes(size: ImageSize): Result<ByteArray> {
        return runCatching {
            val defaultPath = getDefaultImagePath(size)

            if (defaultPath.exists()) {
                return@runCatching defaultPath.readBytes()
            }

            val defaultBitmap = createDefaultBitmap()
            val byteArray = ByteArrayOutputStream().use { stream ->
                defaultBitmap.compress(
                    imageConfig.format.toCompressFormat(),
                    80,
                    stream
                )
                stream.toByteArray()
            }

            if (!defaultPath.parentFile!!.exists()) {
                defaultPath.parentFile?.mkdirs()
            }
            defaultPath.outputStream().write(byteArray)

            byteArray
        }
    }

    override suspend fun openAndResizeImage(
        sourceUriString: String,
        sizeGroup: List<ImageSize>,
        compressionQuality: Int,
    ): Result<EncodedImage> = runCatching {
        val uri = sourceUriString.toUri()
        val resolver = context.contentResolver
        val exif = resolver.exif(uri)

        val maxBound = sizeGroup.maxOf { maxOf(it.width, it.height) }
        val decoded = resolver.downSampling(uri, maxBound)
        val rotated = decoded.rotateByExif(exif)

        if(rotated != decoded) decoded.recycle()

        val images = sizeGroup.associateWith { size ->
            val target = when (size) {
                ImageSize.SMALL -> rotated.scaleCropToFill(size)
                ImageSize.NORMAL -> rotated.scaleToFitInside(size)
            }

            val bytes = ByteArrayOutputStream().use { stream ->
                target.compress(imageConfig.format.toCompressFormat(), compressionQuality, stream)
                stream.toByteArray()
            }

            if (target != rotated) target.recycle()

            bytes
        }

        rotated.recycle()

        EncodedImage(images = images)
    }

    override suspend fun getExif(imageUriString: String): Result<ExifData> {
        return runCatching {
            context.contentResolver.openInputStream(imageUriString.toUri())?.use { stream ->
                exifReader.read(stream)
            } ?: error("openInputStream returned null for uri: $imageUriString")
        }
    }

    override suspend fun getPreview(imageUriString: String): Result<FilePreview> {
        return runCatching {
            val uri = imageUriString.toUri()
            var fileName: String? = null
            var fileSize: Long? = null
            val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    fileName = it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                    fileSize = it.getLong(it.getColumnIndexOrThrow(OpenableColumns.SIZE))
                }
            }
            FilePreview(fileName, fileSize)
        }
    }

    override suspend fun getMediaImages(offset: Int, limit: Int): Result<List<MediaImage>> {
        return runCatching {
            context.contentResolver
                .query(offset, limit)
                ?.toMediaImages()
                ?:emptyList()
        }
    }

    override suspend fun saveGalleryPhotos(uriStrings: List<String>): Result<List<Long>> = runCatching {
        val keyed = uriStrings.associateBy { buildExistingKey(it) }
        val existing = photoDao.findExistingKeys(keyed.keys.toList()).toSet()
        val targets = keyed.filterKeys { it !in existing }
        if (targets.isEmpty()) return@runCatching emptyList()
        val entities = createEntity(targets)
        val rowIds = photoDao.insertAll(entities)

        entities.filterIndexed { i, entity ->
            val inserted = rowIds.getOrNull(i) != -1L
            if (!inserted) {
                ImageSize.entries.forEach { removeImage(entity.imageId, it) }
            }
            inserted
        }.map { it.id }
    }

    override suspend fun loadGalleyPhotos(): Result<List<GalleryPhoto>> {
        return runCatching {
            photoDao.getAll().mapNotNull {
                it.toGalleryPhoto(it.imageId)
            }
        }
    }

    private fun ContentResolver.createExifData(uriString: String): ExifData?{
        val uri = uriString.toUri()
        return openInputStream(uri)?.use { exifReader.read(it) }
    }

    suspend fun Map<ImageSize, ByteArray>.saveImages(imageId: String) : File {
        return  coroutineScope {
            map { (size, bytes) ->
                async { saveImage(bytes, imageId, size).getOrThrow() }
            }.awaitAll().first()
        }
    }

    override suspend fun updateGalleryPhotos(photos: List<GalleryPhoto>): Result<Unit> {
        return runCatching {
            photoDao.updateGalleryPhotos(photos)
        }
    }

    override suspend fun clearGalleryPhotos(ids: Set<Long>): Result<Set<Long>> {
        return runCatching {
            val removed= buildSet {
                val photos= photoDao.getByIds(ids)
                coroutineScope {
                    photos.map { entity ->
                        async {
                            removeImage(entity.imageId, ImageSize.NORMAL).getOrNull()?:return@async
                            removeImage(entity.imageId, ImageSize.SMALL).getOrNull()?:return@async
                            add(entity.id)
                        }
                    }.awaitAll()
                }
            }

            photoDao.deleteByIds(removed)
            removed
        }
    }

    private suspend fun createEntity(targets: Map<String, String>): List<PhotoEntity> {
        val semaphore = Semaphore(10)
        return coroutineScope {
            val geocoder = Geocoder(context, Locale.KOREA)
            targets.map { (sourceKey, uriString) ->
                async {
                    semaphore.withPermit {
                        runCatching {
                            val imageId = generateImageId()
                            val exif = context.contentResolver.createExifData(uriString)
                            val (lat, lng) = exif?.latitude to exif?.longitude

                            val fileDeferred = async {
                                openAndResizeImage(uriString, ImageSize.entries)
                                    .getOrThrow()
                                    .images
                                    .saveImages(imageId)
                            }

                            val addressDeferred = async {
                                if (lat != null && lng != null) {
                                    geocoder.getFromLocation(lat, lng, 1)?.firstOrNull()
                                } else null
                            }


                            PhotoEntity(
                                id = generateId(),
                                imageId = imageId,
                                fileName = fileDeferred.await().name,
                                sourceKey = sourceKey,
                                dateTaken = exif?.timestampMillis,
                                width = exif?.imageWidth,
                                height = exif?.imageHeight,
                                latitude = lat,
                                longitude = lng,
                                address = addressDeferred.await()?.getAddressLine(0)
                            )
                        }.getOrNull()
                    }
                }
            }.awaitAll().filterNotNull()
        }
    }

    private suspend fun PhotoEntity.toGalleryPhoto(imageId:String): GalleryPhoto? {
        val uriString= getImage(imageId, ImageSize.NORMAL).toUri().path?:return null
        val thumbnail= getImage(imageId, ImageSize.SMALL).toUri().path?:return null

        return GalleryPhoto(
            id = id,
            imageId = imageId,
            uriString = uriString,
            thumbnail= thumbnail,
            exif = PhotoExif(
                dateTaken = dateTaken,
                width = width,
                height = height,
                location = if (latitude != null && longitude != null)
                    LatLng(latitude, longitude) else null,
            ),
            courseId = courseId,
            courseName = courseName,
            address = address
        )
    }


    private fun ContentResolver.query(offset: Int, limit: Int): Cursor? {
        val projection = arrayOf(MediaStore.Images.Media._ID)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val args = bundleOf(
                ContentResolver.QUERY_ARG_SORT_COLUMNS to arrayOf(MediaStore.Images.Media.DATE_ADDED),
                ContentResolver.QUERY_ARG_SORT_DIRECTION to ContentResolver.QUERY_SORT_DIRECTION_DESCENDING,
                ContentResolver.QUERY_ARG_LIMIT to limit,
                ContentResolver.QUERY_ARG_OFFSET to offset,
            )
            query(mediaUri, projection, args, null)
        } else {
            val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC LIMIT $limit OFFSET $offset"
            query(mediaUri, projection, null, null, sortOrder)
        }
    }

    private fun Cursor.toMediaImages(): List<MediaImage> {
        return use { cursor ->
            val idCol = getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            buildList(count) {
                while (moveToNext()) {
                    val id = getLong(idCol)
                    val uri = ContentUris.withAppendedId(mediaUri, id).toString()
                    add(MediaImage(id, uri))
                }
            }
        }
    }

    private fun ImageFormat.toCompressFormat(): Bitmap.CompressFormat {
        return when (this) {
            ImageFormat.JPEG -> Bitmap.CompressFormat.JPEG
            ImageFormat.WEBP -> Bitmap.CompressFormat.WEBP
        }
    }

    private fun getDefaultImagePath(size: ImageSize): File {
        return File(
            imageFile.parentFile,
            "default/${size.pathName}/default.$ext"
        )
    }

    private fun createDefaultBitmap(): Bitmap {
        return createBitmap(100, 100).apply {
            val canvas = android.graphics.Canvas(this)
            val paint = android.graphics.Paint().apply { color = 0xFFE7F1DD.toInt() } // 회색
            canvas.drawRect(0f, 0f, width.toFloat(), width.toFloat(), paint)
        }
    }


    private fun buildExistingKey(uriString: String): String =
        uriString.toUri().lastPathSegment ?: uriString.toUri().toString()

    private var lastId = 0L
    private fun generateId(): Long {
        val now = System.currentTimeMillis()
        lastId = if (now <= lastId) lastId + 1 else now
        return lastId
    }

    private fun generateImageId():String = "IM${ULID().nextULID()}"
}