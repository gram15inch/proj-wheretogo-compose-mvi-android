package com.wheretogo.presentation.model

import com.wheretogo.domain.OverlayType
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.presentation.MarkerIconType

data class OverlayTag(
    val overlayId: String,
    val parentId: String = "",
    val overlayType: OverlayType = OverlayType.NONE,
    val iconType: MarkerIconType = MarkerIconType.DEFAULT,
    val latlng: LatLng = LatLng()
) {
    companion object
}