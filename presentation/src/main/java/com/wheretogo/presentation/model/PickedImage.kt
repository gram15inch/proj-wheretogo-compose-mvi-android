package com.wheretogo.presentation.model

import android.net.Uri
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.util.MediaImage

data class PickedImage(
    val id: Long,
    val uri: Uri,
    val latLng: LatLng? = null
){

    fun toMarkerImage(): MediaImage {
        return MediaImage(
            id = id,
            uri.toString()
        )
    }
}