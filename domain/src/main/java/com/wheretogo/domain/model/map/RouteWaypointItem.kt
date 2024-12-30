package com.wheretogo.domain.model.map

data class RouteWaypointItem(
    val alias: String = "",
    val address: String = "",
    val latlng: LatLng = LatLng(),
)