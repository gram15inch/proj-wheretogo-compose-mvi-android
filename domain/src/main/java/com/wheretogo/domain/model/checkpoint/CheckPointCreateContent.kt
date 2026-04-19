package com.wheretogo.domain.model.checkpoint

import com.wheretogo.domain.model.address.LatLng

data class CheckPointCreateContent(
    val courseId: String = "",
    val latLng: LatLng = LatLng(),
    val imageId: String = "",
    val description: String = ""
)