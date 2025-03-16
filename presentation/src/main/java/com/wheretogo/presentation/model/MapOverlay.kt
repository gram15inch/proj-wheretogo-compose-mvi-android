package com.wheretogo.presentation.model

import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.PathOverlay
import com.wheretogo.domain.OverlayType
import com.wheretogo.presentation.MarkerIconType

data class MapOverlay(
    val overlayId: String = "",
    val overlayType: OverlayType = OverlayType.COURSE,
    val iconType: MarkerIconType = MarkerIconType.DEFAULT,
    val markerGroup: List<Marker> = emptyList(),
    val path: PathOverlay? = null
){
    override fun equals(other: Any?): Boolean {
        return other is MapOverlay && this.overlayId == other.overlayId
    }

    override fun hashCode(): Int {
        return overlayId.hashCode()
    }
}
