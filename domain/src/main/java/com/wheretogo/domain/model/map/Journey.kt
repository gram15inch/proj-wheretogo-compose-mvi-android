package com.wheretogo.domain.model.map

data class Journey(
    val code: Int = -1,
    val courseName: String = "",
    val duration: String = "",
    val tag: List<Int> = emptyList(),
    val imgUrl: String = "",
    val course: Course = Course(),
    val checkPoints: List<CheckPoint> = emptyList(),
    val points: List<LatLng> = emptyList()
)
