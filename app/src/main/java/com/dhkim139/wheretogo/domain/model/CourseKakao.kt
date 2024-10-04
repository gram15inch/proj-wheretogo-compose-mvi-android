package com.dhkim139.wheretogo.domain.model

import com.kakao.vectormap.LatLng

data class CourseKakao(
    val start: LatLng,
    val goal: LatLng,
    val waypoints: List<LatLng>
)

