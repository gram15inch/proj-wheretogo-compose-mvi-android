package com.wheretogo.presentation.intent

import com.wheretogo.presentation.feature.GroupingStrategy
import com.wheretogo.presentation.model.PickerImage

sealed interface GalleryIntent {
    data class Initialize(val openPicker: Boolean) : GalleryIntent
    data object Refresh : GalleryIntent
    data class MediaPicked(val images: List<PickerImage>) : GalleryIntent
    data class ChangeGrouping(val strategy: GroupingStrategy) : GalleryIntent
    data class OpenDetail(val id: Long) : GalleryIntent
    data object CloseDetail : GalleryIntent
    data object PhotoAddClick : GalleryIntent
    data class PhotoClick(val pickerId: Long) : GalleryIntent
    data class PhotoLongClick(val pickerId: Long) : GalleryIntent
    data object ClearSelection : GalleryIntent
    data object SelectAll : GalleryIntent
    data object DeleteSelected : GalleryIntent
    data object DismissPicker : GalleryIntent
}