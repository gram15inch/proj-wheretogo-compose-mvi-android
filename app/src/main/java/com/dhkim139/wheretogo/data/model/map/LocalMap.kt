package com.dhkim139.wheretogo.data.model.map

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dhkim139.wheretogo.domain.model.LatLng

@Entity(
    tableName = "LocalMap",
    indices = [Index(value = ["code"], unique = true)]
)
data class LocalMap(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val code: Int = 0,
    var course: Course,
    var points: List<LatLng>,
)
