package com.wheretogo.presentation.state

import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.util.Viewport
import com.wheretogo.presentation.CameraUpdateSource
import com.wheretogo.presentation.MoveAnimation

data class CameraState(
    val latLng: LatLng = LatLng(),
    val zoom: Double = 0.0,
    val viewport: Viewport = Viewport(),
    val updateSource: CameraUpdateSource = CameraUpdateSource.USER,
    val moveAnimation: MoveAnimation = MoveAnimation.APP_LINEAR,
    val isMyLocation: Boolean = false
)