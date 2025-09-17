package com.wheretogo.domain.model.util

import com.wheretogo.domain.ImageSize

data class Image(val imageId:String, val uriPathGroup: Map<ImageSize, String>)
