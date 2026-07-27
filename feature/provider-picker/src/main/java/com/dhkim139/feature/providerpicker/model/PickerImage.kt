package com.dhkim139.feature.providerpicker.model

import android.net.Uri
import androidx.core.net.toUri
import com.wheretogo.domain.model.util.MediaImage

data class PickerImage(
    val id: Long,
    val uri: Uri,
) {
    companion object {
        fun MediaImage.toPickerImage(): PickerImage {
            return PickerImage(
                id = id,
                uri = uriString.toUri()
            )
        }
    }
}