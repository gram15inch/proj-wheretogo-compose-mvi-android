package com.wheretogo.domain.model.util

import com.wheretogo.domain.model.address.LatLng

data class MediaImage(
    val id: Long,
    val uriString: String,
    val latLng: LatLng? = null
)