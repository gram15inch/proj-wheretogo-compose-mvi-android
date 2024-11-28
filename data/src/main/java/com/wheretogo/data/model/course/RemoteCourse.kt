package com.wheretogo.data.model.course

import com.wheretogo.data.DATA_NULL
import com.wheretogo.data.model.map.DataLatLng

data class RemoteCourse(
    val courseId: String = DATA_NULL,
    val courseName: String = "",
    val mapLatLngGroup: List<DataLatLng> = emptyList(),
    val metaCheckPoint: RemoteMetadata = RemoteMetadata(),
    val duration: String = "",
    val tag: String = "",
    val level: String = "",
    val relation: String = "",
    val cameraLatLng: DataLatLng = DataLatLng(),
    val zoom: String = ""
)
