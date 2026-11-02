package com.wheretogo.data.model.gallery

import kotlinx.serialization.Serializable

@Serializable
data class ImageMetaResponse(
    val imageId: String = "",
    val userId: String = "",
    val sha256: String = "",
    val exif: ExifResponse = ExifResponse(),
    val address: String = "",
    val createAt: Long = 0
)