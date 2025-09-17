package com.wheretogo.data.model.route

import com.wheretogo.data.model.map.DataLatLng

data class RemoteRoute(
    val courseId: String = "",
    val userId: String = "",
    val duration: Int = 0,
    val distance: Int = 0,
    val points: List<DataLatLng> = emptyList()
)