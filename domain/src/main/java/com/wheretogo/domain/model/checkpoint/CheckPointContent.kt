package com.wheretogo.domain.model.checkpoint

import com.wheretogo.domain.model.address.LatLng

data class CheckPointContent(
    val groupId: String,
    val imgUriString: String,
    val latLng: LatLng,
    val description: String,
)