package com.wheretogo.data.model.journey

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wheretogo.data.model.map.DataLatLng

@Entity()
data class LocalCourse(
    @PrimaryKey
    val code: Int = -1,
    val start: DataLatLng = DataLatLng(),
    val goal: DataLatLng = DataLatLng(),
    val waypoints: List<DataLatLng> = emptyList()
)
