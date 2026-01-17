package com.wheretogo.presentation.state

data class NaverMapState(
    val latestCameraState: CameraState = CameraState(),
    val requestCameraState: CameraState = CameraState(),
)