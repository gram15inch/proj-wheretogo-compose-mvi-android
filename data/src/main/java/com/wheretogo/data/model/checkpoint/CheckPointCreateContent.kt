package com.wheretogo.data.model.checkpoint

import com.wheretogo.data.model.map.DataLatLng


data class CheckPointCreateContent(
    val courseId: String = "",
    val latLng: DataLatLng = DataLatLng(),
    val imageId: String = "",
    val description: String = ""
)