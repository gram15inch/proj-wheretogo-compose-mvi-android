package com.wheretogo.domain.model.util

import com.wheretogo.domain.ImageSize

data class ImageUris(val imageId:String, val uriPathGroup: Map<ImageSize, String>)
