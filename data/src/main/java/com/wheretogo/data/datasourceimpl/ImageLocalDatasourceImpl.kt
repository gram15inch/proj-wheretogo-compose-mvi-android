package com.wheretogo.data.datasourceimpl

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.exifinterface.media.ExifInterface
import com.wheretogo.data.ImageFormat
import com.wheretogo.data.datasource.ImageLocalDatasource
import com.wheretogo.data.feature.PhotoExifReader
import com.wheretogo.data.model.confg.ImageConfig
import com.wheretogo.domain.ImageSize
import com.wheretogo.domain.feature.fit
import com.wheretogo.domain.feature.rotate
import com.wheretogo.domain.feature.scale
import com.wheretogo.domain.feature.scaleCrop
import com.wheretogo.domain.model.util.ExifData
import com.wheretogo.domain.model.util.FilePreview
import com.wheretogo.domain.model.util.MediaImage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject

class ImageLocalDatasourceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imageFile: File,
    private val imageConfig: ImageConfig,
    private val exifReader: PhotoExifReader
) : ImageLocalDatasource {
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


    override suspend fun removeImage(imageId: String, size: ImageSize): Result<Unit> {
        return runCatching {
            val localFile = getImage(imageId, size)
            if (localFile.exists())
                localFile.delete()
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
        compressionQuality: Int
    ): Result<List<Pair<ImageSize, ByteArray>>> {

        return runCatching {
            val originalBitmap =
                context.contentResolver.openInputStream(sourceUriString.toUri())
                    ?.use { inputStream ->
                        BitmapFactory.decodeStream(inputStream)
                    }!!

            val exif =
                context.contentResolver.openInputStream(sourceUriString.toUri())
                    ?.use { inputStream ->
                        ExifInterface(inputStream)
                    }!!

            coroutineScope {
                sizeGroup.map { size ->
                    async {
                        val newBitmap = originalBitmap
                            .rotate(exif)
                            .scale(size)
                            .run {
                                when (size) {
                                    ImageSize.SMALL -> scaleCrop()
                                    ImageSize.NORMAL -> fit(ImageSize.NORMAL)
                                }
                            }

                        size to ByteArrayOutputStream().use { stream ->
                            newBitmap.compress(
                                imageConfig.format.toCompressFormat(),
                                compressionQuality,
                                stream
                            )
                            stream.toByteArray()
                        }
                    }
                }.awaitAll()
            }
        }
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
            queryImages(offset, limit)?.use { cursor ->
                cursor.toMediaImages()
            } ?: emptyList()
        }
    }

    private fun queryImages(offset: Int, limit: Int): Cursor? {
        val projection = arrayOf(MediaStore.Images.Media._ID)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val args = bundleOf(
                ContentResolver.QUERY_ARG_SORT_COLUMNS to arrayOf(MediaStore.Images.Media.DATE_ADDED),
                ContentResolver.QUERY_ARG_SORT_DIRECTION to ContentResolver.QUERY_SORT_DIRECTION_DESCENDING,
                ContentResolver.QUERY_ARG_LIMIT to limit,
                ContentResolver.QUERY_ARG_OFFSET to offset,
            )
            context.contentResolver.query(mediaUri, projection, args, null)
        } else {
            val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC LIMIT $limit OFFSET $offset"
            context.contentResolver.query(mediaUri, projection, null, null, sortOrder)
        }
    }

    private fun Cursor.toMediaImages(): List<MediaImage> {
        val idCol = getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        return buildList(count) {
            while (moveToNext()) {
                val id = getLong(idCol)
                val uri = ContentUris.withAppendedId(mediaUri, id).toString()
                add(MediaImage(id, uri))
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
}