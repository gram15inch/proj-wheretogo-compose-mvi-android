package com.wheretogo.presentation.state

import com.wheretogo.domain.model.map.CameraState

data class NaverMapState(
    val latestCameraState: CameraState = CameraState(),
    val isZoomControl: Boolean = false
)