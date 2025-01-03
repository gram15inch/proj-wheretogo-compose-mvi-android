package com.wheretogo.presentation.model

import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.PathOverlay
import com.wheretogo.domain.OverlayType

data class MapOverlay(
    val overlayId: String = "",
    val type: OverlayType = OverlayType.COURSE,
    val markerGroup: List<Marker> = emptyList(),
    val path: PathOverlay? = null
)
