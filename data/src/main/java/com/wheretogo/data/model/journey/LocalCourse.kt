package com.wheretogo.data.model.journey

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity()
data class LocalCourse(
    @PrimaryKey
    val code: Int = -1,
    val start: LocalLatLng = LocalLatLng(),
    val goal: LocalLatLng = LocalLatLng(),
    val waypoints: List<LocalLatLng> = emptyList()
)
