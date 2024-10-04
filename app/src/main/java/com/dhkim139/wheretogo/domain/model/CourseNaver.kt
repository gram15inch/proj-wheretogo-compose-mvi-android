package com.dhkim139.wheretogo.domain.model

import com.naver.maps.geometry.LatLng as NaverLatLng

data class CourseNaver(
    val start: NaverLatLng,
    val goal: NaverLatLng,
    val waypoints: List<NaverLatLng>
)

