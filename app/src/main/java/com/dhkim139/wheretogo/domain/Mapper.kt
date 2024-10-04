package com.dhkim139.wheretogo.domain

import com.dhkim139.wheretogo.data.model.map.Course
import com.dhkim139.wheretogo.domain.model.CourseKakao
import com.dhkim139.wheretogo.domain.model.CourseNaver
import com.naver.maps.geometry.LatLng as NaverLatLng
import com.kakao.vectormap.LatLng as KakaoLatLng
import com.dhkim139.wheretogo.domain.model.LatLng as LocalLatLng

fun Course.toNaver(): CourseNaver {
    return CourseNaver(
        start = NaverLatLng(start.latitude, start.longitude),
        goal = NaverLatLng(goal.latitude, goal.longitude),
        waypoints = waypoints.toNaver())
}

fun Course.toKakao(): CourseKakao {
    return CourseKakao(
        start = com.kakao.vectormap.LatLng.from(start.latitude, start.longitude),
        goal = com.kakao.vectormap.LatLng.from(goal.latitude, goal.longitude),
        waypoints = waypoints.toKakao())
}


fun List<LocalLatLng>.toNaver():List<NaverLatLng>{
   return this.map { NaverLatLng(it.latitude, it.longitude) }
}

fun List<LocalLatLng>.toKakao():List<KakaoLatLng>{
    return this.map { KakaoLatLng.from(it.latitude, it.longitude) }
}