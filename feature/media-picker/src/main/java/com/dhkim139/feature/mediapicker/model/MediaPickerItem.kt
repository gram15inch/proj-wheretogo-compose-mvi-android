package com.dhkim139.feature.mediapicker.model

import android.net.Uri
import androidx.core.net.toUri
import com.wheretogo.domain.model.util.MediaImage

data class MediaPickerItem(
    val id: Long,
    val uri: Uri
) {
    companion object {
        fun MediaImage.toPickerImage(): MediaPickerItem {
            return MediaPickerItem(
                id = id,
                uri = uriString.toUri()
            )
        }
    }
}