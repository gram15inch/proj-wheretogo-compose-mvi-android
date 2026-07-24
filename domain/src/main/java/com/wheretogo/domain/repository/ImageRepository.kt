package com.wheretogo.domain.repository

import com.wheretogo.domain.ImageSize
import com.wheretogo.domain.model.util.ExifData
import com.wheretogo.domain.model.util.FilePreview
import com.wheretogo.domain.model.util.ImageUris
import com.wheretogo.domain.model.util.MediaImage
import com.wheretogo.domain.model.gallery.GalleryPhoto
import kotlinx.coroutines.flow.Flow

interface ImageRepository {
    suspend fun getImage(imageId: String, size: ImageSize): Result<String>
    suspend fun setImage(imgUriString: String): Result<ImageUris>
    suspend fun removeImage(imageId: String): Result<Unit>
    suspend fun removeLocalImage(imageId: String): Result<Unit>
    suspend fun removeImageWithMeta(imageId: String): Result<Unit>

    suspend fun setRemoteImage(imageId: String): Result<ImageUris>
    suspend fun setRemoteImageWithMeta(imageId: String, userId:String): Result<ImageUris>
    suspend fun removeRemoteImage(imageId: String): Result<Unit>

    suspend fun getExif(imageUriString: String): Result<ExifData>
    suspend fun getPreview(imageUriString: String): Result<FilePreview>
    suspend fun getMediaImages(offset: Int, limit: Int): Result<List<MediaImage>>

    suspend fun getGalleryPhotosByImageId(imageIds: List<String>): Result<List<GalleryPhoto>>
    fun observeGalleryPhotos(): Flow<List<GalleryPhoto>>
    fun observeStampedGalleryPhotos(limit: Int): Flow<List<GalleryPhoto>>
    suspend fun refreshGalleyPhotosByUserId(userId: String):Result<Unit>
    suspend fun saveGalleryPhotos(imgUriStrings: List<String>): Result<List<Long>>
    suspend fun updateGalleryPhotos(photos: List<GalleryPhoto>): Result<Unit>
    suspend fun clearStampAt(imageIds: List<String>): Result<Unit>
    suspend fun clearGalleryPhotos(ids: Set<Long>): Result<Set<Long>>
}