package com.wheretogo.domain.model

data class Course(val code: Int, val start: LatLng, val goal: LatLng, val waypoints: List<LatLng>) {
    companion object {
        fun empty() = Course(0, LatLng(0.0, 0.0), LatLng(0.0, 0.0), emptyList())
    }
}

