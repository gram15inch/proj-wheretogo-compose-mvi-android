package com.wheretogo.domain.model.map

import com.wheretogo.domain.model.address.LatLng

data class MoveCameraOption(
    val latlng: LatLng? = null,
    val zoom: Double? = null,
    val trigger: CameraMoveTrigger = CameraMoveTrigger.DEFAULT,
    val animation: MoveAnimation = MoveAnimation.APP_LINEAR,
    val targetId: String? = null,
    val isMyLocation: Boolean = false
)