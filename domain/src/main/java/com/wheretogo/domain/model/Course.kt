package com.wheretogo.domain.model

data class Course(val code: Int, val start: LatLng, val goal: LatLng, val waypoints: List<LatLng>)
