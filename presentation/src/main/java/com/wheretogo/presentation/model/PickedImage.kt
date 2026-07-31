package com.wheretogo.presentation.model

import android.net.Uri
import com.dhkim139.feature.providerpicker.model.PickerImage
import com.wheretogo.domain.model.util.MediaImage

data class PickedImage(
    val id: Long,
    val uri: Uri
){

    fun toMarkerImage(): MediaImage {
        return MediaImage(
            id = id,
            uri.toString()
        )
    }
}