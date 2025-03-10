package com.wheretogo.domain.model.map

data class CourseAddRequest(
    val courseName: String = "",
    val waypoints: List<LatLng> = emptyList(),
    val points: List<LatLng> = emptyList(),
    val duration: String = "",
    val type: String = "",
    val level: String = "",
    val relation: String = "",
    val cameraLatLng: LatLng = LatLng(),
    val zoom: String = "",
)