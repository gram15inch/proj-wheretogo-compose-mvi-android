package com.dhkim139.feature.providerpicker.model

import android.net.Uri
import androidx.core.net.toUri
import com.wheretogo.domain.model.util.MediaImage

data class ProviderPickerItem(
    val id: Long,
    val uri: Uri,
) {
    companion object {
        fun MediaImage.toPickerImage(): ProviderPickerItem {
            return ProviderPickerItem(
                id = id,
                uri = uriString.toUri()
            )
        }
    }
}