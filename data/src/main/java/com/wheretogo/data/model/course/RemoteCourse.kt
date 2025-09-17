package com.wheretogo.data.model.course

import com.wheretogo.data.DATA_NULL
import com.wheretogo.data.model.map.DataLatLng

data class RemoteCourse(
    val courseId: String = DATA_NULL,
    val courseName: String = "",
    val userId: String = "",
    val userName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val geoHash: String = "",
    val waypoints: List<DataLatLng> = emptyList(),
    val keyword: List<String> = emptyList(),
    val duration: String = "",
    val type: String = "",
    val level: String = "",
    val relation: String = "",
    val cameraLatLng: DataLatLng = DataLatLng(),
    val zoom: String = "",
    val createAt: Long = 0,
)
