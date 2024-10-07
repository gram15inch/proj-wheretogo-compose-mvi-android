package com.wheretogo.presentation

import com.wheretogo.domain.model.Course
import com.wheretogo.domain.model.LatLng

val c1= Course( // 기흥호수공원 순환
    code=1001,
    start = LatLng(37.24049254419747, 127.10069878544695),
    goal = LatLng(37.24022338235744, 127.10061868739378),
    waypoints = listOf(
        LatLng(37.22248268378388, 127.09011137932174)
    )
)

val c2 = Course(// 광교호수공원 순환
    code=1002,
    start= LatLng(37.27671532173225, 127.06655325086643),
    goal = LatLng(37.27657246721216, 127.06654706022783),
    waypoints = listOf(
        LatLng(37.28349073445237, 127.05838075642289)
    )
)


val c3 = Course(// 에버랜드1 장미원 <-> 에버랜드1bw주차장
    code=1003,
    start= LatLng(37.29374581981885, 127.2191664582874),
    goal = LatLng(37.291369283252635, 127.19645446426905),
    waypoints = emptyList()
)

val c4 = Course(// 에버랜드2 장미원 <-> 백련사주차장
    code=1004,
    start= LatLng(37.291369283252635, 127.19645446426905),
    goal = LatLng(37.307017149836575, 127.18157716604357),
    waypoints = emptyList()
)

val c5 = Course(// 기흥역 공영주차장 <-> 역북램프 공영주차장
    code=1005,
    start= LatLng(37.2755481129516, 127.11608496870285),
    goal = LatLng(37.23030242438518, 127.17902628408461),
    waypoints = emptyList()
)

val c6 = Course(// 기흥역 <-> 용인 운전면허시험 <-> 신갈오거리
    code=1006,
    start= LatLng(37.27559271736718, 127.11604329252128),
    goal = LatLng(37.27563974998419, 127.11581720128734),
    waypoints = listOf(
        LatLng(37.28759684879138, 127.10806493673851),
        LatLng(37.27152199721471, 127.10628875008994)
    )
)

val c7 = Course(// 에버랜드1b주차장 <-> 용인 스피드웨이
    code=1007,
    start= LatLng(37.293758966834496, 127.21915696703662),
    goal = LatLng(37.293786578277825, 127.21912319980723),
    waypoints = listOf(LatLng(37.297128542121285, 127.21101658110999))
)

