package com.dhkim139.wheretogo.data.repository

import com.dhkim139.wheretogo.data.model.map.Course
import com.dhkim139.wheretogo.domain.model.LatLng

val c1= Course( // 기흥호수공원
    start = LatLng(37.24049254419747, 127.10069878544695),
    goal = LatLng(37.24022338235744, 127.10061868739378),
    waypoints = listOf(
        LatLng(37.22248268378388, 127.09011137932174)
    )
)

val c2 = Course(// 광교호수공원
    start=LatLng(37.27671532173225, 127.06655325086643),
    goal = LatLng(37.27657246721216, 127.06654706022783),
    waypoints = listOf(
        LatLng(37.28349073445237, 127.05838075642289)
    )
)