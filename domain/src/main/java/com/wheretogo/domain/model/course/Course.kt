package com.wheretogo.domain.model.course

import com.wheretogo.domain.DOMAIN_EMPTY
import com.wheretogo.domain.model.address.LatLng

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
    val isHide: Boolean = false,
    val zoom: String = "",
    val like: Int = 0,
    val reportedCount: Int = 0,
    val updateAt: Long = 0,
    val createAt: Long = 0
)