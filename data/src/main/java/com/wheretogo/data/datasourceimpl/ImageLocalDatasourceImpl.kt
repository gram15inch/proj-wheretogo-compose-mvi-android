package com.wheretogo.data.datasourceimpl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.wheretogo.data.datasource.ImageLocalDatasource
import com.wheretogo.domain.ImageSize
import com.wheretogo.domain.feature.fit
import com.wheretogo.domain.feature.rotate
import com.wheretogo.domain.feature.scale
import com.wheretogo.domain.feature.scaleCrop
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class ImageLocalDatasourceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imagePath: File
) : ImageLocalDatasource {
    override suspend fun getImage(fileName: String, size: ImageSize): File {
        val localFile = File(imagePath.parentFile, "image/${size.pathName}_${fileName}").apply {
            if (!parentFile!!.exists())
                parentFile?.mkdirs()
        }
        return localFile
    }

    override suspend fun setImage(uri: Uri, fileName: String) {
        saveResizedImagesToCache(context, uri, fileName, ImageSize.entries)
    }

    private suspend fun saveResizedImagesToCache(
        context: Context,
        sourceUri: Uri,
        fileName: String,
        sizes: List<ImageSize>,
        compressionQuality: Int = 80
    ) {

        val originalBitmap =
            context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            } ?: return

        val exif =
            context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                ExifInterface(inputStream)
            } ?: return

        coroutineScope {
            sizes.map { size ->
                async {
                    val newBitmap = originalBitmap
                        .rotate(exif)
                        .scale(size)
                        .run {
                            if (size == ImageSize.SMALL) {
                                scaleCrop()
                            } else {
                                this.fit(ImageSize.NORMAL)
                            }
                        }
                    val localFile = getImage(fileName, size)
                    FileOutputStream(localFile).use { outputStream ->
                        newBitmap.compress(
                            Bitmap.CompressFormat.JPEG,
                            compressionQuality,
                            outputStream
                        )
                    }
                }
            }
        }.awaitAll()
    }

    override suspend fun removeImage(fileName: String, size: ImageSize) {
        val localFile = File(imagePath.parentFile, "image/${size.pathName}_${fileName}")
        if (localFile.exists())
            localFile.delete()
    }
}