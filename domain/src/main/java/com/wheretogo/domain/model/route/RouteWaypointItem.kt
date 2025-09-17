package com.wheretogo.domain.model.route

import com.wheretogo.domain.model.address.LatLng

data class RouteWaypointItem(
    val alias: String = "",
    val address: String = "",
    val latlng: LatLng = LatLng(),
)