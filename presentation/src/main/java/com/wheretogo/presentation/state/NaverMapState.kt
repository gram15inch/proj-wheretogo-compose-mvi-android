package com.wheretogo.presentation.state

import com.wheretogo.presentation.model.MapOverlay

data class NaverMapState(
    val overlayGroup: Collection<MapOverlay> = emptyList(),
    val cameraState: CameraState = CameraState(),
    val isMapReady: Boolean = false
)