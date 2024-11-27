package com.wheretogo.data.model.journey

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.wheretogo.data.model.map.DataLatLng

@Entity(
    tableName = "LocalJourney",
    indices = [
        Index(value = ["latitude", "longitude"])
    ]
)
data class LocalJourney(
    @PrimaryKey
    val code: Int = 0,
    val longitude: Double,
    val latitude: Double,
    var course: LocalCourse,
    val pointsDate : Long,
    var points: List<DataLatLng>,
)