package com.dhkim139.feature.camerapicker.model

import android.net.Uri

data class Capture(
    val uri: Uri,
    val location: Location?,
    val returnedAt: Long,
)