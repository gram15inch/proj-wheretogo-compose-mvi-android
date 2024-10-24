package com.wheretogo.domain.model

data class Course(
    val code: Int = -1,
    val start: LatLng = LatLng(),
    val goal: LatLng = LatLng(),
    val waypoints: List<LatLng> = emptyList()
)