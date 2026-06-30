package com.wheretogo.data.model.gallery

import com.wheretogo.domain.ImageSize

data class EncodedImage(
    val images: Map<ImageSize, ByteArray>,
)