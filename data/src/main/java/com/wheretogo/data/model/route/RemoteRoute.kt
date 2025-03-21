package com.wheretogo.data.model.route

import com.wheretogo.domain.model.map.LatLng

data class RemoteRoute(
    val courseId: String = "",
    val duration: Int = 0,
    val distance: Int = 0,
    val points: List<LatLng> = emptyList()
)