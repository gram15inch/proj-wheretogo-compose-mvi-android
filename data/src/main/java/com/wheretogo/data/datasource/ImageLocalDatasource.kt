package com.wheretogo.data.datasource

import com.wheretogo.data.model.gallery.EncodedImage
import com.wheretogo.domain.ImageSize
import com.wheretogo.domain.model.util.ExifData
import com.wheretogo.domain.model.util.FilePreview
import com.wheretogo.domain.model.util.MediaImage
import com.wheretogo.domain.model.gallery.GalleryPhoto
import java.io.File


interface ImageLocalDatasource {

    suspend fun getImage(imageId: String, size: ImageSize): File

    suspend fun removeImage(imageId: String, size: ImageSize): Result<Boolean>

    suspend fun saveImage(byteArray: ByteArray, imageId: String, size: ImageSize): Result<File>

    suspend fun getDefaultImageBytes(size: ImageSize): Result<ByteArray>

    suspend fun openAndResizeImage(
        sourceUriString: String,
        sizeGroup: List<ImageSize>,
        compressionQuality: Int = 80
    ): Result<EncodedImage>

    suspend fun getExif(imageUriString: String): Result<ExifData>

    suspend fun getPreview(imageUriString: String): Result<FilePreview>

    suspend fun getMediaImages(offset: Int, limit: Int): Result<List<MediaImage>>

    suspend fun loadGalleyPhotos():Result<List<GalleryPhoto>>

    suspend fun saveGalleryPhotos(uriStrings:List<String>): Result<List<Long>>

    suspend fun clearGalleryPhotos(ids: Set<Long>): Result<Set<Long>>
}