package com.wheretogo.data.model.gallery

data class ImageMetaCreateContent(
    val imageId: String,
    val exif: ExifEntity,
    val sha256: String,
    val address: String,
    val createAt: Long,
    val userId: String? = null,
)