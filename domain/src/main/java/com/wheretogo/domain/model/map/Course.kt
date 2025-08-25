package com.wheretogo.domain.model.map

import com.wheretogo.domain.DOMAIN_EMPTY

data class Course(
    val courseId: String = DOMAIN_EMPTY,
    val courseName: String = "",
    val userId: String = DOMAIN_EMPTY,
    val userName: String = DOMAIN_EMPTY,
    val waypoints: List<LatLng> = emptyList(),
    val checkpointIdGroup: List<String> = emptyList(), // 주입
    val points: List<LatLng> = emptyList(),
    val duration: String = "",
    val type: String = "",
    val level: String = "",
    val relation: String = "",
    val cameraLatLng: LatLng = LatLng(),
    val isUserCreated: Boolean = false,
    val zoom: String = "",
    val like: Int = 0
    //todo 미니맵 추가
)