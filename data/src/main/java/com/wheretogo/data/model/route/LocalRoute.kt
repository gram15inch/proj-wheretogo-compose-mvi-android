package com.wheretogo.data.model.route

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wheretogo.domain.model.map.LatLng

@Entity(
    tableName = "LocalRoute"
)
data class LocalRoute(
    @PrimaryKey
    val courseId: String = "",
    val duration: Int = 0,
    val distance: Int = 0,
    val points: List<LatLng> = emptyList()
)