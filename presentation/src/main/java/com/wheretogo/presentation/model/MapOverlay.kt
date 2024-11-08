package com.wheretogo.presentation.model

import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.PathOverlay

data class MapOverlay(
    val code: Int,
    val marker: Marker,
    val pathOverlay: PathOverlay
)
