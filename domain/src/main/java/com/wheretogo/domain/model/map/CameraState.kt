package com.wheretogo.domain.model.map

import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.util.Viewport

data class CameraState(
    val latLng: LatLng = LatLng(),
    val zoom: Double = 0.0,
    val viewport: Viewport = Viewport()
)