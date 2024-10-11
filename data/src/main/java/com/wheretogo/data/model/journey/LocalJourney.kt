package com.wheretogo.data.model.journey

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "LocalJourney",
    indices = [
        Index(value = ["code"], unique = true),
        Index(value = ["latitude", "longitude"])
    ]
)
data class LocalJourney(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val code: Int = 0,
    val longitude: Double,
    val latitude: Double,
    var course: LocalCourse,
    var points: List<LocalLatLng>,
)