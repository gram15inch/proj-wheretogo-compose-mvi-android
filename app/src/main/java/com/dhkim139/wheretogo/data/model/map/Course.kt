package com.dhkim139.wheretogo.data.model.map

import com.dhkim139.wheretogo.domain.model.LatLng

data class Course(val code: Int, val start: LatLng, val goal: LatLng, val waypoints: List<LatLng>)
