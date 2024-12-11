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
    val latLng: LatLng = LatLng(),
    val titleComment: String = "",
    val remoteImgUrl: String = "",
    val localImgUrl: String = "",
    val timestamp: Long = 0L
)