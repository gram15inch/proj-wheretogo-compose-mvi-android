package com.wheretogo.presentation.model

import android.net.Uri
import com.dhkim139.feature.providerpicker.model.PickerImage
import com.wheretogo.domain.model.util.MediaImage

data class PickedImage(
    val id: Long,
    val uri: Uri
){

    companion object{
        fun fromPicker(list:List<PickerImage>) = list.map { PickedImage(it.id,it.uri) }
    }

    fun toMarkerImage(): MediaImage {
        return MediaImage(
            id = id,
            uri.toString()
        )
    }
}