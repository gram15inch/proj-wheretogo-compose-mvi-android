package com.wheretogo.presentation.feature

import android.content.Context
import android.net.Uri
import java.io.File


fun formatFileSizeToMB(bytes: Long): String {
    val mb = bytes / (1024.0 * 1024.0)
    return "%.1f MB".format(mb)
}

fun getAssetFileUri(context: Context, assetFileName: String): Uri? {
    val assetManager = context.assets
    val file = File.createTempFile("test_image", ".jpg_pb")

    try {
        assetManager.open(assetFileName).use { inputStream ->
            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }

    return Uri.fromFile(file)
}