package com.wheretogo.presentation.model

import com.wheretogo.domain.ZOOM
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.course.Course
import com.wheretogo.domain.model.map.CameraMoveTrigger
import com.wheretogo.domain.model.map.MoveAnimation

data class CameraOption(
    val latLng: LatLng,
    val zoom: Double,
    val updateSource: CameraMoveTrigger,
    val moveAnimation: MoveAnimation,
    val isMyLocation: Boolean = false
) {

    companion object {
        fun fromCourse(course: Course): CameraOption {
            return CameraOption(
                latLng = course.cameraLatLng,
                zoom = ZOOM.Place.level,
                updateSource = CameraMoveTrigger.DEFAULT,
                moveAnimation = MoveAnimation.APP_JUMP,
                isMyLocation = false
            )
        }
    }
}