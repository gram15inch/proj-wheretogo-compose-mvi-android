package com.wheretogo.presentation.state

import com.wheretogo.domain.model.map.CameraState
import com.wheretogo.presentation.model.CameraOption

data class NaverMapState(
    val latestCameraState: CameraState = CameraState(),
    val initCamera: CameraOption? = null
)