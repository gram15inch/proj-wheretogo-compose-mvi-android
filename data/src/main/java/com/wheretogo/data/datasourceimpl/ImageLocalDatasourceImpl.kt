package com.wheretogo.data.datasourceimpl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import com.wheretogo.data.ImageFormat
import com.wheretogo.data.datasource.ImageLocalDatasource
import com.wheretogo.data.model.confg.ImageConfig
import com.wheretogo.domain.ImageSize
import com.wheretogo.domain.feature.fit
import com.wheretogo.domain.feature.rotate
import com.wheretogo.domain.feature.scale
import com.wheretogo.domain.feature.scaleCrop
import com.wheretogo.domain.usecase.util.ExifData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class ImageLocalDatasourceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imageFile: File,
    private val imageConfig: ImageConfig
) : ImageLocalDatasource {
    private val ext = imageConfig.format.ext
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
                val exif = ExifInterface(stream)

                val latlng = exif.latLong
                val hasLatLong = latlng != null
                val altitude = if (exif.hasAttribute(ExifInterface.TAG_GPS_ALTITUDE)) {
                    exif.getAltitude(0.0)
                } else null

                val dateTimeStr = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
                    ?: exif.getAttribute(ExifInterface.TAG_DATETIME)
                val timestamp = dateTimeStr?.let { parseExifDateTime(it) }

                ExifData(
                    latitude = if (hasLatLong) latlng[0] else null,
                    longitude = if (hasLatLong) latlng[1] else null,
                    altitude = altitude,
                    dateTimeOriginal = dateTimeStr,
                    timestampMillis = timestamp,
                    make = exif.getAttribute(ExifInterface.TAG_MAKE),
                    model = exif.getAttribute(ExifInterface.TAG_MODEL),
                    orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    ),
                    imageWidth = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0)
                        .takeIf { it > 0 },
                    imageHeight = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0)
                        .takeIf { it > 0 },
                    fNumber = exif.getAttribute(ExifInterface.TAG_F_NUMBER),
                    exposureTime = exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME),
                    iso = exif.getAttribute(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY),
                    focalLength = exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH)
                )
            }!!
        }
    }

    private fun parseExifDateTime(dateTime: String): Long? {
        return try {
            val format = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.KOREA)
            format.parse(dateTime)?.time
        } catch (e: Exception) {
            null
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