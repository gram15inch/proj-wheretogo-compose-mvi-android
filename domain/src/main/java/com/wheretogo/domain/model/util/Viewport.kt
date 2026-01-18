package com.wheretogo.domain.model.util

import com.wheretogo.domain.model.address.LatLng

data class Viewport(
    val northWest: LatLng = LatLng(),
    val northEast: LatLng = LatLng(),
    val southWest: LatLng = LatLng(),
    val southEast: LatLng = LatLng(),
    val center: LatLng = LatLng()
)