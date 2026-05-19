package com.wheretogo.domain.model.util

import com.wheretogo.domain.model.address.LatLng

data class Navigation(val courseName: String, val waypoints: List<LatLng>)