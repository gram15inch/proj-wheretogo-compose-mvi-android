package com.wheretogo.domain.usecase.gallery

import com.wheretogo.domain.model.gallery.GalleryPhoto
import kotlinx.coroutines.flow.Flow

interface LoadGalleryPhotosUseCase {
    fun observe(): Flow<List<GalleryPhoto>>
    fun observeStamped(limit: Int): Flow<List<GalleryPhoto>>
    suspend fun groupRefresh(forceRefresh: Boolean = false): Result<Unit>
}