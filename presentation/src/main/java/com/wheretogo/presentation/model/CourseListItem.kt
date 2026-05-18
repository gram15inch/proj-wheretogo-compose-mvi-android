package com.wheretogo.presentation.model

import com.wheretogo.domain.model.course.Course


enum class StartDirection{
    FORWARD, REVERSE
}

data class CourseListItem(val direction: StartDirection, val course: Course){

    fun toCourseByDirection():Course{
        return when(direction){
            StartDirection.FORWARD -> course.copy(
                cameraLatLng = course.points.firstOrNull()?:course.cameraLatLng
            )
            StartDirection.REVERSE -> course.copy(
                cameraLatLng = course.points.lastOrNull()?:course.cameraLatLng
            )
        }
    }
}