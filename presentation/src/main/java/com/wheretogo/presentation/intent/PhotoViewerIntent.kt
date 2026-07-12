package com.wheretogo.presentation.intent

import com.wheretogo.domain.model.gallery.GalleryPhoto
import com.wheretogo.domain.model.map.CameraState
import com.wheretogo.presentation.AppLifecycle

sealed class PhotoViewerIntent {

    data class Refresh(val photo: GalleryPhoto) : PhotoViewerIntent()
    data class Stamp(val photo: GalleryPhoto, val description: String?) : PhotoViewerIntent()
    data class RemoveStamp(val photo: GalleryPhoto) : PhotoViewerIntent()
    data class CameraUpdate(val camera: CameraState) : PhotoViewerIntent()

    //공통
    data class LifecycleChange(val event: AppLifecycle) : PhotoViewerIntent()
}