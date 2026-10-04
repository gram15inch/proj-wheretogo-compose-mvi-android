package com.wheretogo.presentation.model

import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.route.Direction

data class PolylineInfo(
    val contentId: String,
    val direction: Direction = Direction.FORWARD,
    val points: List<LatLng>,
    val isVisible: Boolean = true
)