package com.wheretogo.presentation.model


import com.wheretogo.domain.model.Course
import com.wheretogo.domain.model.LatLng
import com.kakao.vectormap.LatLng as KakaoLatLng
import com.naver.maps.geometry.LatLng as NaverLatLng

fun Course.toNaver(): CourseNaver {
    return CourseNaver(
        start = NaverLatLng(start.latitude, start.longitude),
        goal = NaverLatLng(goal.latitude, goal.longitude),
        waypoints = waypoints.toNaver()
    )
}

fun Course.toKakao(): CourseKakao {
    return CourseKakao(
        start = KakaoLatLng.from(start.latitude, start.longitude),
        goal = KakaoLatLng.from(goal.latitude, goal.longitude),
        waypoints = waypoints.toKakao()
    )
}


fun List<LatLng>.toNaver(): List<NaverLatLng> {
    return this.map { NaverLatLng(it.latitude, it.longitude) }
}

fun List<LatLng>.toKakao(): List<KakaoLatLng> {
    return this.map { KakaoLatLng.from(it.latitude, it.longitude) }
}

fun NaverLatLng.toDomainLatLng(): LatLng {
    return LatLng(latitude, longitude)
}
fun LatLng.toNaver(): NaverLatLng {
    return NaverLatLng(latitude, longitude)
}
