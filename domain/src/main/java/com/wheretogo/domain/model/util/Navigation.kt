package com.wheretogo.domain.model.util

import com.wheretogo.domain.model.course.StartDirection

data class Navigation(
    val courseName: String,
    val waypoints: List<LatLngLabel>,
    val direction: StartDirection
)