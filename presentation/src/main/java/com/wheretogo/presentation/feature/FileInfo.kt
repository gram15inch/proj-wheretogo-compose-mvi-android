package com.wheretogo.presentation.feature

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns


fun getFileInfoFromUri(contentResolver: ContentResolver, uri: Uri): Pair<String?, Long?> {
    var fileName: String? = null
    var fileSize: Long? = null
    val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            fileName = it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
            fileSize = it.getLong(it.getColumnIndexOrThrow(OpenableColumns.SIZE))
        }
    }
    return Pair(fileName, fileSize)
}

fun formatFileSizeToMB(bytes: Long): String {
    val mb = bytes / (1024.0 * 1024.0)
    return "%.1f MB".format(mb)
}

