package com.wheretogo.data.feature

import androidx.exifinterface.media.ExifInterface
import com.wheretogo.data.model.gallery.ExifEntity
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class PhotoExifReader @Inject constructor() {

    fun read(input: InputStream): ExifEntity = ExifInterface(input).toExifData()

    fun read(file: File): ExifEntity = ExifInterface(file.absolutePath).toExifData()

    private fun ExifInterface.toExifData(): ExifEntity {
        val latLong = FloatArray(2)
        val hasLocation = getLatLong(latLong)

        val altitude = if (hasAttribute(ExifInterface.TAG_GPS_ALTITUDE)) {
            getAltitude(0.0)
        } else null

        val rawW = getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0).takeIf { it > 0 }
        val rawH = getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0).takeIf { it > 0 }

        val orientation =
            getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        val swapped = orientation == ExifInterface.ORIENTATION_ROTATE_90 ||
                orientation == ExifInterface.ORIENTATION_ROTATE_270 ||
                orientation == ExifInterface.ORIENTATION_TRANSPOSE ||
                orientation == ExifInterface.ORIENTATION_TRANSVERSE

        val displayW = if (swapped) rawH else rawW
        val displayH = if (swapped) rawW else rawH

        val dateTimeStr = getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
            ?: getAttribute(ExifInterface.TAG_DATETIME)
        val timestamp = dateTimeStr?.let { parseExifDateTime(it) }

        return ExifEntity(
            latitude = if (hasLocation) latLong[0].toDouble() else null,
            longitude = if (hasLocation) latLong[1].toDouble() else null,
            altitude = altitude,
            dateTimeOriginal = dateTimeStr,
            timestampMillis = timestamp,
            make = getAttribute(ExifInterface.TAG_MAKE),
            model = getAttribute(ExifInterface.TAG_MODEL),
            orientation = orientation,
            imageWidth = displayW,
            imageHeight = displayH,
            fNumber = getAttribute(ExifInterface.TAG_F_NUMBER),
            exposureTime = getAttribute(ExifInterface.TAG_EXPOSURE_TIME),
            iso = getAttribute(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY),
            focalLength = getAttribute(ExifInterface.TAG_FOCAL_LENGTH)
        )
    }

    private fun parseExifDateTime(raw: String): Long? = runCatching {
        SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.KOREA).parse(raw)?.time
    }.getOrNull()
}