package com.wheretogo.data.datasource

import com.wheretogo.data.model.gallery.EncodedImage
import com.wheretogo.data.model.gallery.ExifEntity
import com.wheretogo.data.model.gallery.PhotoEntity
import com.wheretogo.domain.ImageSize
import com.wheretogo.domain.model.util.FilePreview
import com.wheretogo.domain.model.util.MediaImage
import kotlinx.coroutines.flow.Flow
import java.io.File


interface ImageLocalDatasource {

    suspend fun getImage(imageId: String, size: ImageSize): File

    suspend fun removeImage(imageId: String, size: ImageSize): Result<Boolean>

    suspend fun saveImage(byteArray: ByteArray, imageId: String, size: ImageSize): Result<File>

    suspend fun getDefaultImageBytes(size: ImageSize): Result<ByteArray>

    suspend fun encodeImage(
        sourceUriString: String,
        sizeGroup: List<ImageSize>,
        compressionQuality: Int = 80
    ): Result<EncodedImage>

    suspend fun getExif(imageUriString: String): Result<ExifEntity>

    suspend fun getPreview(imageUriString: String): Result<FilePreview>

    suspend fun getMediaImages(offset: Int, limit: Int): Result<List<MediaImage>>

    suspend fun loadAllPhotos(): Result<List<PhotoEntity>>

    fun observePhotos(): Flow<List<PhotoEntity>>

    suspend fun getPhotosByHash(hashes:List<String>): Result<List<PhotoEntity>>

    suspend fun getPhotosByImageId(imageIds: List<String>): Result<List<PhotoEntity>>

    suspend fun saveGalleryPhotos(uriStrings: List<String>): Result<List<Long>>

    suspend fun upsertPhotos(photos: List<PhotoEntity>): Result<List<Long>>

    suspend fun updatePhotos(photos: List<PhotoEntity>): Result<Unit>

    suspend fun clearStampAt(imageIds: List<String>): Result<Unit>

    suspend fun clearGalleryPhotos(ids: Set<Long>): Result<Set<Long>>
}