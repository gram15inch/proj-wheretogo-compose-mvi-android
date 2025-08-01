package com.wheretogo.data.model.course

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.wheretogo.data.DATA_NULL
import com.wheretogo.data.model.map.DataLatLng


@Entity(
    tableName = "LocalCourse",
    indices = [Index(value = ["geoHash"])]
)
data class LocalCourse(
    @PrimaryKey
    val courseId: String = DATA_NULL,
    val courseName: String = "",
    val userId: String = DATA_NULL,
    val userName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val geoHash: String = "",
    val waypoints: List<DataLatLng> = emptyList(),
    val checkpointSnapshot: LocalSnapshot = LocalSnapshot(),
    val duration: String = "",
    val type: String = "",
    val level: String = "",
    val relation: String = "",
    val cameraLatLng: DataLatLng = DataLatLng(),
    val zoom: String = "",
    val like: Int = 0,
)
