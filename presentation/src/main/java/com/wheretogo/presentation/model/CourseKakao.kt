package com.wheretogo.presentation.model

import com.kakao.vectormap.LatLng as KakaoLatLng
data class CourseKakao(
    val start: KakaoLatLng,
    val goal: KakaoLatLng,
    val waypoints: List<KakaoLatLng>
)

