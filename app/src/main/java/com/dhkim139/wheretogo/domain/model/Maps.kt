package com.dhkim139.wheretogo.domain.model

import com.dhkim139.wheretogo.data.model.map.Course
import com.naver.maps.geometry.LatLng as NaverLatLng
import com.kakao.vectormap.LatLng as KakaoLatLng

data class CourseNaver(
    val start: NaverLatLng,
    val goal: NaverLatLng,
    val waypoints: List<NaverLatLng>
)

data class CourseKakao(
    val start: KakaoLatLng,
    val goal: KakaoLatLng,
    val waypoints: List<KakaoLatLng>
)

fun Course.toNaver(): CourseNaver {
    return CourseNaver(
        start = NaverLatLng(start.latitude, start.longitude),
        goal = NaverLatLng(goal.latitude, goal.longitude),
        waypoints = waypoints.map { NaverLatLng(it.latitude, it.longitude) })
}

fun Course.toKakao(): CourseKakao {
    return CourseKakao(
        start = KakaoLatLng.from(start.latitude, start.longitude),
        goal = KakaoLatLng.from(goal.latitude, goal.longitude),
        waypoints = waypoints.map { KakaoLatLng.from(it.latitude, it.longitude) })
}