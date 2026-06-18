package com.wheretogo.domain.model.gallery

import com.wheretogo.domain.model.address.LatLng

data class PhotoExif(
    val dateTaken: Long? = null,
    val width: Int? = null,
    val height: Int? = null,
    val location: LatLng? = null,
)