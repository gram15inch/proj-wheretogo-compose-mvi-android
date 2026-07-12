package com.wheretogo.data.model.gallery

data class ImageMetaResponse(
    val imageId: String = "",
    val userId: String = "",
    val sha256: String = "",
    val exif: ExifResponse = ExifResponse(),
    val address: String = "",
    val createAt: Long = 0
)