package com.wheretogo.domain.model.util

import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.course.StartDirection

data class Navigation(val courseName: String, val waypoints: List<LatLng>, val direction: StartDirection)