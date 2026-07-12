package com.wheretogo.data.model.gallery

import com.wheretogo.domain.model.util.ExifData

data class ImageMetaCreateContent(
    val imageId: String,
    val exif: ExifData,
    val sha256: String,
    val address: String,
    val createAt: Long,
    val userId: String? = null,
)