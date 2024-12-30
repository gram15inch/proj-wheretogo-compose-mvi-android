package com.wheretogo.domain.model.map

data class Route(
    val courseId: String = "",
    val waypointItems: List<RouteWaypointItem> = emptyList(),
    val duration: Int = 0,
    val distance: Int = 0,
    val points: List<LatLng> = emptyList()
)