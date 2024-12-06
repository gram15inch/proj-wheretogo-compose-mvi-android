package com.wheretogo.data.model.route

import com.wheretogo.domain.model.map.LatLng

data class RemoteRoute(
    val courseId: String = "",
    val points: List<LatLng> = emptyList()
)