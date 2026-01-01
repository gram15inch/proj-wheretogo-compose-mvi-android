package com.wheretogo.presentation.state

data class NaverMapState(
    val cameraState: CameraState = CameraState(),
    val isMyLocation : Boolean = false
)