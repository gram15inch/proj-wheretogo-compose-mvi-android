package com.wheretogo.data.feature

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.core.graphics.scale
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
    val srcW = (size.width / scaleFactor).toInt().coerceAtMost(width)
    val srcH = (size.height / scaleFactor).toInt().coerceAtMost(height)
    val srcX = ((width - srcW) / 2).coerceAtLeast(0)
    val srcY = ((height - srcH) / 2).coerceAtLeast(0)

    val matrix = Matrix().apply { postScale(scaleFactor, scaleFactor) }
    return Bitmap.createBitmap(this, srcX, srcY, srcW, srcH, matrix, true)
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

fun ContentResolver.downSampling(uri: Uri, maxBound: Int): Bitmap {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }

    var sample = 1
    val marginRatio = 2
    val largest = maxOf(bounds.outWidth, bounds.outHeight)
    while (largest / sample > maxBound * marginRatio) sample *= marginRatio

    val opts = BitmapFactory.Options().apply {
        inSampleSize = sample
        inPreferredConfig = Bitmap.Config.RGB_565
    }
    return openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
        ?: error("이미지 로드 실패: $uri")
}

fun ContentResolver.exif(
    uri: Uri
): ExifInterface {
    return openInputStream(uri)?.use { ExifInterface(it) }
        ?: error("exif 로드 실패: $uri")
}