package com.wheretogo.presentation.state

import androidx.annotation.StringRes
import com.wheretogo.domain.model.gallery.GalleryPhoto
import com.wheretogo.presentation.R
import com.wheretogo.presentation.model.PhotoSection

sealed interface GalleryState {
    data object Loading : GalleryState
    data class Success(
        val sections: List<PhotoSection> = emptyList(),
        val selectedIds: Set<Long> = emptySet(),
        @StringRes val groupingLabel: Int = R.string.unknown,
    ) : GalleryState {
        val isSelectionMode: Boolean get() = selectedIds.isNotEmpty()
        val allPhotos: List<GalleryPhoto> get() = sections.flatMap { it.photos }
    }
    data object Empty : GalleryState
    data class Error(val message: String) : GalleryState

    suspend fun<T> onSuccessAwait(callback: suspend (Success)-> T): T?{
        return if(this is Success)
            callback(this)
        else
            null
    }

    fun<T> onSuccess(callback: (Success)-> T): T?{
        return if(this is Success)
            callback(this)
        else
            null
    }
}