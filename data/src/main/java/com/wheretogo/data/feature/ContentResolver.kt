package com.wheretogo.data.feature

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.InputStream
import java.security.MessageDigest


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


fun ContentResolver.sha256(
    uri: Uri
): String {
    return openInputStream(uri)?.use { it.sha256() }?: error("md5 로드 실패: $uri")
}

private fun InputStream.sha256(bufferSize: Int = 65536): String {
    val md = MessageDigest.getInstance("SHA-256")
    buffered(bufferSize).use { input ->
        val buffer = ByteArray(bufferSize)
        var read: Int
        while (input.read(buffer).also { read = it } != -1) {
            md.update(buffer, 0, read)
        }
    }
    return md.digest().joinToString("") { "%02x".format(it) }
}