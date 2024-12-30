package com.wheretogo.presentation.state

import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.model.map.Viewport
import com.wheretogo.presentation.CameraStatus

data class CameraState(
    val latLng: LatLng = LatLng(),
    val zoom: Double = 0.0,
    val viewport: Viewport = Viewport(),
    val status: CameraStatus = CameraStatus.NONE
)