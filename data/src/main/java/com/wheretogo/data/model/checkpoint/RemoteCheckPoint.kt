package com.wheretogo.data.model.checkpoint

import com.wheretogo.data.model.map.DataLatLng

data class RemoteCheckPoint(
    val checkPointId: String = "",
    val courseId: String = "",
    val userId: String = "",
    val userName : String = "",
    val latLng: DataLatLng = DataLatLng(),
    val caption: String = "",
    val imageName: String = "",
    val description: String = ""
)