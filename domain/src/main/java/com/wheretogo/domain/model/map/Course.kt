package com.wheretogo.domain.model.map

import com.wheretogo.domain.DOMAIN_EMPTY

data class Course(
    val courseId: String = DOMAIN_EMPTY,
    val courseName: String = "",
    val waypoints: List<LatLng> = emptyList(),
    val checkpoints: List<CheckPoint> = emptyList(), // 주입
    val points: List<LatLng> = emptyList(), // 주입
    val duration: String = "",
    val tag: String = "",
    val level: String = "",
    val relation: String = "",
    val cameraLatLng: LatLng = LatLng(),
    val zoom: String = "",
    val like: Int = 0 // 주입
    //todo 미니맵 추가
)