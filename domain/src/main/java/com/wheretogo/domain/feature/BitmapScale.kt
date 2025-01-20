package com.wheretogo.domain.feature

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import androidx.exifinterface.media.ExifInterface
import com.wheretogo.domain.ImageSize


fun Bitmap.fit(size: ImageSize): Bitmap {
    val squareBitmap = Bitmap.createBitmap(size.width, size.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(squareBitmap)
    val paint = Paint().apply { color = Color.BLACK }

    canvas.drawRect(0f, 0f, size.width.toFloat(), size.height.toFloat(), paint)

    val left = (size.width - this.width) / 2
    val top = (size.height - this.height) / 2
    canvas.drawBitmap(this, left.toFloat(), top.toFloat(), null)
    return squareBitmap
}

fun Bitmap.scale(size: ImageSize): Bitmap {
    val scaleFactor = if (width > height) {
        size.width.toFloat() / width
    } else {
        size.height.toFloat() / height
    }

    val resizedWidth = (width * scaleFactor).toInt()
    val resizedHeight = (height * scaleFactor).toInt()

    return Bitmap.createScaledBitmap(this, resizedWidth, resizedHeight, true)
}


fun Bitmap.scaleCrop(): Bitmap {
    val squareSize = minOf(width, height)
    val xOffset = (width - squareSize) / 2
    val yOffset = (height - squareSize) / 2
    return Bitmap.createBitmap(this, xOffset, yOffset, squareSize, squareSize)
}

fun Bitmap.resize(size: ImageSize): Bitmap {
    return Bitmap.createScaledBitmap(this, size.width, size.height, true)
}

fun Bitmap.rotate(exif: ExifInterface): Bitmap {
    val rotate =
        exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

    val rotationDegrees = when (rotate) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90
        ExifInterface.ORIENTATION_ROTATE_180 -> 180
        ExifInterface.ORIENTATION_ROTATE_270 -> 270
        else -> 0
    }
    val matrix = Matrix()
    matrix.postRotate(rotationDegrees.toFloat())
    return Bitmap.createBitmap(this, 0, 0, this.width, this.height, matrix, true)
}
