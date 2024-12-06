package com.wheretogo.data.model.meta

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "LocalMetaGeoHash")
data class LocalMetaGeoHash(
    @PrimaryKey
    val geoHash: String,
    val timestamp: Long
)