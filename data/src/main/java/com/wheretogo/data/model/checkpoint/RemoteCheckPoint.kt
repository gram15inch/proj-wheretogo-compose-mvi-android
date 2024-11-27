package com.wheretogo.data.model.checkpoint

import com.wheretogo.data.model.map.DataLatLng

data class RemoteCheckPoint(
    val checkPointId: String = "",
    val latLng: DataLatLng = DataLatLng(),
    val titleComment: String = "",
    val imgUrl: String = ""
)