package com.wheretogo.data.model.course

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.wheretogo.data.DATA_NULL

import com.wheretogo.domain.model.map.LatLng


@Entity(
    tableName = "LocalCourse",
    indices = [Index(value = ["geoHash"])]
)
data class LocalCourse(
    @PrimaryKey
    val courseId: String = DATA_NULL,
    val courseName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val geoHash: String = "",
    val waypoints: List<LatLng> = emptyList(),
    val route: List<LatLng> = emptyList(),
    val metaCheckPoint: DataMetaCheckPoint = DataMetaCheckPoint(),
    val duration: String = "",
    val tag: String = "",
    val level: String = "",
    val relation: String = "",
    val cameraLatLng: LatLng = LatLng(),
    val zoom: String = "",
    val like: Int = 0,
)
