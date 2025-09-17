package com.wheretogo.domain.model.course

import com.wheretogo.domain.model.address.LatLng

data class CourseContent(
    val courseName: String = "",
    val waypoints: List<LatLng> = emptyList(),
    val points: List<LatLng> = emptyList(),
    val duration: String = "",
    val type: String = "",
    val level: String = "",
    val relation: String = "",
    val cameraLatLng: LatLng = LatLng(),
    val zoom: String = ""
)