package com.wheretogo.data.model.route

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wheretogo.data.model.map.DataLatLng

@Entity(
    tableName = "LocalRoute"
)
data class LocalRoute(
    @PrimaryKey
    val courseId: String = "",
    val duration: Int = 0,
    val distance: Int = 0,
    val points: List<DataLatLng> = emptyList()
)