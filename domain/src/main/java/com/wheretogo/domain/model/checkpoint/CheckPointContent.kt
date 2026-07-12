package com.wheretogo.domain.model.checkpoint

import com.wheretogo.domain.model.address.LatLng

data class CheckPointContent(
    val groupId: String,
    val latLng: LatLng,
    val description: String,
    val imgUriString: String? = null,
    val imageId: String? = null // 이미 존재하는 이미지로 추가
)