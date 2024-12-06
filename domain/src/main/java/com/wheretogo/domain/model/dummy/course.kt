package com.wheretogo.domain.model.dummy

import com.wheretogo.domain.model.map.Course
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.model.map.MetaCheckPoint


fun getDomainCourseDummy(): List<Course> {

    val w1 = listOf(
        // 기흥호수공원 순환
        LatLng(37.24049254419747, 127.10069878544695),
        LatLng(37.22248268378388, 127.09011137932174),
        LatLng(37.24022338235744, 127.10061868739378),
    )

    val w2 = listOf( // 광교호수공원 순환
        LatLng(37.27671532173225, 127.06655325086643),
        LatLng(37.28349073445237, 127.05838075642289),
        LatLng(37.27657246721216, 127.06654706022783)
    )

    val w3 = listOf(
        // 에버랜드1 장미원 <-> 에버랜드1bw주차장
        LatLng(37.29374581981885, 127.2191664582874),
        LatLng(37.291369283252635, 127.19645446426905),
    )

    val w4 = listOf(
        // 에버랜드2 장미원 <-> 백련사주차장
        LatLng(37.291369283252635, 127.19645446426905),
        LatLng(37.307017149836575, 127.18157716604357),
    )

    val w5 = listOf(
        // 기흥역 공영주차장 <-> 역북램프 공영주차장
        LatLng(37.2755481129516, 127.11608496870285),
        LatLng(37.23030242438518, 127.17902628408461),
    )

    val w6 = listOf(
        // 기흥역 <-> 용인 운전면허시험 <-> 신갈오거리
        LatLng(37.27559271736718, 127.11604329252128),
        LatLng(37.28759684879138, 127.10806493673851),
        LatLng(37.27152199721471, 127.10628875008994),
        LatLng(37.27563974998419, 127.11581720128734),
    )

    val w7 = listOf(
        // 에버랜드1b주차장 <-> 용인 스피드웨이
        LatLng(37.293758966834496, 127.21915696703662),
        LatLng(37.297128542121285, 127.21101658110999),
        LatLng(37.293786578277825, 127.21912319980723),
    )

    return listOf(
        Course(
            courseId = "cs1",
            courseName = "기흥호수공원 순환",
            waypoints = w1,
            metaCheckPoint = getMetaCheckPointDummy("c1"),
            duration = "15",
            tag = "드라이브",
            level = "",
            relation = "가족",
            cameraLatLng = w1.first(),
            zoom = ""
        ),
        Course(
            courseId = "cs2",
            courseName = "광교호수공원 순환",
            waypoints = w2,
            metaCheckPoint = getMetaCheckPointDummy("c2"),
            duration = "15",
            tag = "드라이브",
            level = "",
            relation = "친구",
            cameraLatLng = w2.first(),
            zoom = ""
        ),
        Course(
            courseId = "cs3",
            courseName = "에버랜드 장미원 <-> 1bw 주차장",
            waypoints = w3,
            metaCheckPoint = getMetaCheckPointDummy(),
            duration = "15",
            tag = "스포츠",
            level = "3",
            relation = "단독",
            cameraLatLng = w3.first(),
            zoom = ""
        ),
        Course(
            courseId = "cs4",
            courseName = "에버랜드 장미원 <-> 백련사",
            waypoints = w4,
            metaCheckPoint = getMetaCheckPointDummy(),
            duration = "15",
            tag = "스포츠",
            level = "2",
            relation = "단독",
            cameraLatLng = w4.first(),
            zoom = ""
        ),
        Course(
            courseId = "cs5",
            courseName = "기흥역 <-> 역북램프 공영주차장",
            waypoints = w5,
            metaCheckPoint = getMetaCheckPointDummy(),
            duration = "15",
            tag = "도로연수",
            level = "1",
            relation = "단독",
            cameraLatLng = w5.first(),
            zoom = ""
        ),

        Course(
            courseId = "cs6",
            courseName = "기흥역 <-> 용인 운전면허시험 <-> 신갈오거리",
            waypoints = w6,
            metaCheckPoint = getMetaCheckPointDummy("c6"),
            duration = "15",
            tag = "도로연수",
            level = "1",
            relation = "단독",
            cameraLatLng = w6.first(),
            zoom = ""
        ),

        Course(
            courseId = "cs7",
            courseName = "에버랜드1b주차장 <-> 용인 스피드웨이",
            waypoints = w7,
            metaCheckPoint = getMetaCheckPointDummy(),
            duration = "15",
            tag = "스포츠",
            level = "4",
            relation = "단독",
            cameraLatLng = w7.first(),
            zoom = ""
        ),
    )
}


fun getMetaCheckPointDummy(courseId: String = ""): MetaCheckPoint {
    val list = mutableListOf<String>()
    when (courseId) {
        "cs1" -> {
            (1..4).forEach {
                list.add("cp$it")
            }
        }

        "cs2" -> {
            (5..8).forEach {
                list.add("cp$it")
            }
        }

        "cs6" -> {
            (9..12).forEach {
                list.add("cp$it")
            }
        }
    }

    return MetaCheckPoint(
        metaCheckPointGroup = list,
        timeStamp = System.currentTimeMillis()
    )
}