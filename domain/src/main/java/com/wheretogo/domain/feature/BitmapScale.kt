package com.wheretogo.domain.feature

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import com.wheretogo.domain.ImageSize

fun Bitmap.scaleToFitInside(size: ImageSize): Bitmap {
    val scaleFactor = minOf(
        size.width.toFloat() / width,
        size.height.toFloat() / height,
    ).coerceAtMost(1f)   // 업스케일 방지

    val newW = (width * scaleFactor).toInt().coerceAtLeast(1)
    val newH = (height * scaleFactor).toInt().coerceAtLeast(1)
    if (newW == width && newH == height) return this
    return Bitmap.createScaledBitmap(this, newW, newH, true)
}

fun Bitmap.scaleCropToFill(size: ImageSize): Bitmap {
    val scaleFactor = maxOf(
        size.width.toFloat() / width,
        size.height.toFloat() / height,
    )
    val scaledW = (width * scaleFactor).toInt().coerceAtLeast(size.width)
    val scaledH = (height * scaleFactor).toInt().coerceAtLeast(size.height)
    val scaled =
        if (scaledW == width && scaledH == height) this
        else Bitmap.createScaledBitmap(this, scaledW, scaledH, true)

    val xOffset = ((scaled.width - size.width) / 2).coerceAtLeast(0)
    val yOffset = ((scaled.height - size.height) / 2).coerceAtLeast(0)
    val cropW = size.width.coerceAtMost(scaled.width)
    val cropH = size.height.coerceAtMost(scaled.height)
    return Bitmap.createBitmap(scaled, xOffset, yOffset, cropW, cropH)
}

fun Bitmap.rotateByExif(exif: ExifInterface): Bitmap {
    val orientation =
        exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
        ExifInterface.ORIENTATION_TRANSPOSE -> { matrix.postRotate(90f); matrix.postScale(-1f, 1f) }
        ExifInterface.ORIENTATION_TRANSVERSE -> { matrix.postRotate(270f); matrix.postScale(-1f, 1f) }
        else -> return this   // NORMAL/UNDEFINED → 회전 불필요, 원본 반환
    }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

