package com.dhkim139.feature.camerapicker.model

import android.net.Uri

data class VerifiedImage(
    val id: Long,
    val uri: Uri,
    val isVerified: Boolean,
    val location: Location?,
)

