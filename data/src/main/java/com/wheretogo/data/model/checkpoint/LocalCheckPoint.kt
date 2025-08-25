package com.wheretogo.data.model.checkpoint

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wheretogo.data.DATA_NULL
import com.wheretogo.data.model.map.DataLatLng

@Entity(
    tableName = "LocalCheckPoint"
)
data class LocalCheckPoint(
    @PrimaryKey
    val checkPointId: String = DATA_NULL,
    val courseId:String = "",
    val userId: String = "",
    val userName :String = "",
    val latLng: DataLatLng = DataLatLng(),
    val captionId: String = "",
    val caption: String = "",
    val imageName: String = "",
    val imageLocalPath: String = "",
    val description: String = "",
    val timestamp: Long = 0L
)