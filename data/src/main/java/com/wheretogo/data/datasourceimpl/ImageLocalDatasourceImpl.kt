package com.wheretogo.data.datasourceimpl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.net.toUri
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
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject

class ImageLocalDatasourceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imageFile: File
) : ImageLocalDatasource {
    override suspend fun getImage(fileName: String, size: ImageSize): File {
        val localFile = File(imageFile.parentFile, "image/${size.pathName}/${fileName}").apply {
            if (!parentFile!!.exists()) {
                parentFile?.mkdirs()
            }
        }
        return localFile
    }

    override suspend fun saveImage(byteArray: ByteArray, fileName: String, size: ImageSize): Uri {
        val file = getImage(fileName, size)
        file.outputStream().use { steam ->
            steam.write(byteArray)
        }
        return file.toUri()
    }


    override suspend fun removeImage(fileName: String, size: ImageSize) {
        val localFile = getImage(fileName, size)
        if (localFile.exists())
            localFile.delete()
    }


    override suspend fun openAndResizeImage(
        sourceUri: Uri,
        sizeGroup: List<ImageSize>,
        compressionQuality: Int
    ): List<Pair<ImageSize, ByteArray>> {

        val originalBitmap =
            context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }!!

        val exif =
            context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                ExifInterface(inputStream)
            }!!


        return coroutineScope {
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
                            Bitmap.CompressFormat.JPEG,
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