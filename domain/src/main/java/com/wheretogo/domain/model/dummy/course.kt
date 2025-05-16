package com.wheretogo.domain.model.dummy

import com.wheretogo.domain.RouteAttrItem
import com.wheretogo.domain.feature.hashSha256
import com.wheretogo.domain.model.map.CheckPoint
import com.wheretogo.domain.model.map.Course
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.model.map.RouteCategory
import com.wheretogo.domain.model.user.Profile
import com.wheretogo.domain.model.user.ProfilePrivate


fun getWaypointDummy(courseId:String):List<LatLng>{
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

   return  when(courseId){
       "cs1"->w1
       "cs2"->w2
       "cs3"->w3
       "cs4"->w4
       "cs5"->w5
       "cs6"->w6
       "cs7"->w7
       else-> emptyList()
   }
}

fun getCourseDummy(): List<Course> {
    val user1 = getProfileDummy()[0]
    return listOf(
        Course(
            courseId = "cs1",
            courseName = "기흥호수공원 순환",
            userId = user1.uid,
            waypoints = getWaypointDummy("cs1"),
            points = getWaypointDummy("cs1"),
            checkpointIdGroup = getCheckPointDummy("cs1").map { it.checkPointId },
            duration = "20",
            type = RouteCategory.fromItem(RouteAttrItem.DRIVE)?.code.toString(),
            level = RouteCategory.fromItem(RouteAttrItem.BEGINNER)?.code.toString(),
            relation = RouteCategory.fromItem(RouteAttrItem.FAMILY)?.code.toString(),
            cameraLatLng = getWaypointDummy("cs1").first(),
            zoom = ""
        ),
        Course(
            courseId = "cs2",
            courseName = "광교호수공원 순환",
            userId = user1.uid,
            waypoints = getWaypointDummy("cs2"),
            points = getWaypointDummy("cs2"),
            checkpointIdGroup = getCheckPointDummy("cs2").map { it.checkPointId },
            duration = "25",
            type = RouteCategory.fromItem(RouteAttrItem.DRIVE)?.code.toString(),
            level = RouteCategory.fromItem(RouteAttrItem.BEGINNER)?.code.toString(),
            relation = RouteCategory.fromItem(RouteAttrItem.FRIEND)?.code.toString(),
            cameraLatLng = getWaypointDummy("cs2").first(),
            zoom = ""
        ),
        Course(
            courseId = "cs3",
            courseName = "에버랜드 장미원 <-> 1bw 주차장",
            userId = user1.uid,
            waypoints = getWaypointDummy("cs3"),
            points = getWaypointDummy("cs3"),
            checkpointIdGroup = getCheckPointDummy().map { it.checkPointId },
            duration = "13",
            type = RouteCategory.fromItem(RouteAttrItem.SPORT)?.code.toString(),
            level = RouteCategory.fromItem(RouteAttrItem.EXPERT)?.code.toString(),
            relation = RouteCategory.fromItem(RouteAttrItem.SOLO)?.code.toString(),
            cameraLatLng = getWaypointDummy("cs3").first(),
            zoom = ""
        ),
        Course(
            courseId = "cs4",
            courseName = "에버랜드 장미원 <-> 백련사",
            userId = user1.uid,
            waypoints = getWaypointDummy("cs4"),
            points = getWaypointDummy("cs4"),
            checkpointIdGroup = getCheckPointDummy().map { it.checkPointId },
            duration = "4",
            type = RouteCategory.fromItem(RouteAttrItem.SPORT)?.code.toString(),
            level = RouteCategory.fromItem(RouteAttrItem.LOVER)?.code.toString(),
            relation = RouteCategory.fromItem(RouteAttrItem.SOLO)?.code.toString(),
            cameraLatLng = getWaypointDummy("cs4").first(),
            zoom = ""
        ),
        Course(
            courseId = "cs5",
            courseName = "기흥역 <-> 역북램프 공영주차장",
            userId = user1.uid,
            waypoints = getWaypointDummy("cs5"),
            points = getWaypointDummy("cs5"),
            checkpointIdGroup = getCheckPointDummy().map { it.checkPointId },
            duration = "7",
            type = RouteCategory.fromItem(RouteAttrItem.TRAINING)?.code.toString(),
            level = RouteCategory.fromItem(RouteAttrItem.BEGINNER)?.code.toString(),
            relation = RouteCategory.fromItem(RouteAttrItem.SOLO)?.code.toString(),
            cameraLatLng = getWaypointDummy("cs5").first(),
            zoom = ""
        ),

        Course(
            courseId = "cs6",
            courseName = "기흥역 <-> 용인 운전면허시험 <-> 신갈오거리",
            userId = user1.uid,
            waypoints = getWaypointDummy("cs6"),
            points = getWaypointDummy("cs6"),
            checkpointIdGroup = getCheckPointDummy("cs6").map { it.checkPointId },
            duration = "5",
            type = RouteCategory.fromItem(RouteAttrItem.TRAINING)?.code.toString(),
            level = RouteCategory.fromItem(RouteAttrItem.BEGINNER)?.code.toString(),
            relation = RouteCategory.fromItem(RouteAttrItem.SOLO)?.code.toString(),
            cameraLatLng = getWaypointDummy("cs6").first(),
            zoom = ""
        ),

        Course(
            courseId = "cs7",
            courseName = "에버랜드1b주차장 <-> 용인 스피드웨이",
            userId = user1.uid,
            waypoints = getWaypointDummy("cs7"),
            points = getWaypointDummy("cs7"),
            checkpointIdGroup = getCheckPointDummy().map { it.checkPointId },
            duration = "12",
            type = RouteCategory.fromItem(RouteAttrItem.SPORT)?.code.toString(),
            level = RouteCategory.fromItem(RouteAttrItem.PRO)?.code.toString(),
            relation = RouteCategory.fromItem(RouteAttrItem.SOLO)?.code.toString(),
            cameraLatLng = getWaypointDummy("cs7").first(),
            zoom = ""
        ),
    )
}

fun getCheckPointDummy(courseId: String = ""): List<CheckPoint> {
    val list = mutableListOf<CheckPoint>()
    val profileGroup = getProfileDummy()
    when (courseId) {
        "cs1" -> {
            getWaypointDummy("cs1").forEachIndexed {i,latlng->
                val user = profileGroup[0]
                list.add(
                    CheckPoint(
                        checkPointId = "cs1_cp${i}",
                        userId = user.uid,
                        userName = user.name,
                        latLng = latlng
                    )
                )
            }
        }

        "cs2" -> {
            val user = profileGroup[1]
            getWaypointDummy("cs2").forEachIndexed {i,latlng->
                list.add(
                    CheckPoint(
                        checkPointId = "cs2_cp${i}",
                        userId = user.uid,
                        userName = user.name,
                        latLng = latlng
                    )
                )
            }
        }

        "cs6" -> {
            val user = profileGroup[2]
            getWaypointDummy("cs6").forEachIndexed {i,latlng->
                list.add(
                    CheckPoint(
                        checkPointId = "cs6_cp${i}",
                        userId = user.uid,
                        userName = user.name,
                        latlng
                    )
                )
            }
        }
    }

    return list
}

fun getProfileDummy():List<Profile> {
    val list = mutableListOf<Profile>()
    (1..3).forEach {
        list.add(
            Profile(
                uid="dummyUid$it",
                name = "dummyName$it",
                hashMail = hashSha256("dummyMail$it@mail.com"),
                private = ProfilePrivate(
                    mail = "dummyMail$it@mail.com",
                    lastVisited = System.currentTimeMillis(),
                    accountCreation = System.currentTimeMillis(),
                )
            )
        )
    }
    list.mapIndexed {i,p->
        when(i){
            0->{
                p.copy(
                    private = p.private.copy(
                        isAdmin = true,
                        isAdRemove = true
                    )
                )
            }
            1->{
                p.copy(
                    private = p.private.copy(
                        isAdmin = false,
                        isAdRemove = true
                    )
                )
            }
            2->{
                p.copy(
                    private = p.private.copy(
                        isAdmin = false,
                        isAdRemove = false
                    )
                )
            }
            else->p
        }
    }
    return list
}
