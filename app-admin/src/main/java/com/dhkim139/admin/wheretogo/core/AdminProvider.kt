package com.dhkim139.admin.wheretogo.core

import android.content.ContentProvider
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.provider.MediaStore

class AdminProvider : ContentProvider() {

   private val authority: String by lazy { "${context!!.packageName}.provider" }

    override fun onCreate() = true

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        queryArgs: Bundle?,
        cancellationSignal: CancellationSignal?,
    ): Cursor? {
        if (uri.pathSegments.firstOrNull() != "list") return null
        val cr = context!!.contentResolver

        val args = (queryArgs ?: Bundle()).apply {
            putStringArray(
                ContentResolver.QUERY_ARG_SORT_COLUMNS,
                arrayOf(MediaStore.Images.Media.DATE_TAKEN),
            )
            putInt(
                ContentResolver.QUERY_ARG_SORT_DIRECTION,
                ContentResolver.QUERY_SORT_DIRECTION_DESCENDING,
            )
        }

        val out = MatrixCursor(arrayOf("_id", "uri", "date_taken"))
        cr.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATE_TAKEN),
            args,
            cancellationSignal,
        )?.use { c ->
            val idCol = c.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val dateCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
            while (c.moveToNext()) {
                val id = c.getLong(idCol)
                out.addRow(listOf(id, "content://$authority/original/$id", c.getLong(dateCol)))
            }
        }
        return out
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        val cr = context!!.contentResolver
        val media: Uri = when (uri.pathSegments.firstOrNull()) {
            "original" -> uri.lastPathSegment?.toLongOrNull()?.let {
                ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, it)
            }
            else -> null
        } ?: return null

        return cr.openFileDescriptor(MediaStore.setRequireOriginal(media), "r")
    }

    override fun getType(uri: Uri) = "image/jpeg"

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?,
    ): Cursor? = query(uri, projection, Bundle(), null)

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, s: String?, a: Array<String>?) = 0
    override fun update(uri: Uri, v: ContentValues?, s: String?, a: Array<String>?) = 0
}