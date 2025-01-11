package com.wheretogo.data.model.course

import com.wheretogo.data.DATA_NULL
import com.wheretogo.domain.model.map.LatLng

data class RemoteCourse(
    val courseId: String = DATA_NULL,
    val courseName: String = "",
    val userId: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val geoHash: String = "",
    val waypoints: List<LatLng> = emptyList(),
    val dataMetaCheckPoint: DataMetaCheckPoint = DataMetaCheckPoint(),
    val duration: String = "",
    val tag: String = "",
    val level: String = "",
    val relation: String = "",
    val cameraLatLng: LatLng = LatLng(),
    val zoom: String = ""
)
