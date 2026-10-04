package com.wheretogo.domain.model.map

import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.course.CameraFocus

data class MoveCameraOption(
    val latlng: LatLng? = null,
    val zoom: Double? = null,
    val trigger: CameraMoveTrigger = CameraMoveTrigger.DEFAULT,
    val animation: MoveAnimation = MoveAnimation.APP_LINEAR,
    val targetId: String? = null,
    val isMyLocation: Boolean = false,
    val focus: CameraFocus? = null
)