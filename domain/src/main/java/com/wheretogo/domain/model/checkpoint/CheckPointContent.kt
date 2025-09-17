package com.wheretogo.domain.model.checkpoint

import com.wheretogo.domain.model.address.LatLng

data class CheckPointContent(
    val courseId: String = "",
    val latLng: LatLng = LatLng(),
    val imageUriString: String = "",
    val description: String = ""
)