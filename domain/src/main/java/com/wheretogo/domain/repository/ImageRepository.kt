package com.wheretogo.domain.repository

import com.wheretogo.domain.ImageSize
import com.wheretogo.domain.model.util.ExifData
import com.wheretogo.domain.model.util.FilePreview
import com.wheretogo.domain.model.util.ImageUris
import com.wheretogo.domain.model.util.MediaImage
import com.wheretogo.domain.model.gallery.GalleryPhoto

interface ImageRepository {
    suspend fun getImage(imageId: String, size: ImageSize): Result<String>
    suspend fun setImage(imgUriString: String): Result<ImageUris>
    suspend fun removeImage(imageId: String): Result<Unit>

    suspend fun getExif(imageUriString: String): Result<ExifData>
    suspend fun getPreview(imageUriString: String): Result<FilePreview>
    suspend fun getMediaImages(offset: Int, limit: Int): Result<List<MediaImage>>

    suspend fun loadGalleyPhotos():Result<List<GalleryPhoto>>
    suspend fun saveGalleryPhotos(imgUriStrings: List<String>): Result<List<Long>>
    suspend fun updateGalleryPhotos(photos: List<GalleryPhoto>): Result<Unit>
    suspend fun clearGalleryPhotos(ids: Set<Long>): Result<Set<Long>>
}