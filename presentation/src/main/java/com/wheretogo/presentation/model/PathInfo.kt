package com.wheretogo.presentation.model

import com.wheretogo.domain.model.address.LatLng

data class PathInfo(
    val contentId: String,
    val points: List<LatLng>,
    val minZoomLevel: Double,
    val isVisible: Boolean = true
)