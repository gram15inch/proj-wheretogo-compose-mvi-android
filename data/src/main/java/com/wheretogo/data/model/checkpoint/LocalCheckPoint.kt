package com.wheretogo.data.model.checkpoint

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wheretogo.data.DATA_NULL
import com.wheretogo.domain.model.map.LatLng

@Entity(
    tableName = "LocalCheckPoint"
)
data class LocalCheckPoint(
    @PrimaryKey
    val checkPointId: String = DATA_NULL,
    val userId: String = "",
    val userName :String = "",
    val latLng: LatLng = LatLng(),
    val caption: String = "",
    val imageName: String = "",
    val imageLocalPath: String = "",
    val description: String = "",
    val timestamp: Long = 0L
)