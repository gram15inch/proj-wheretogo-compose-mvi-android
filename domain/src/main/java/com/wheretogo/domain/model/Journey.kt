package com.wheretogo.domain.model

data class Journey(
    val id: Int = 0,
    val code: Int = 0,
    var course: Course,
    var points: List<LatLng>,
)
