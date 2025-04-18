package com.wheretogo.presentation.model

import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.PathOverlay
import com.wheretogo.presentation.MarkerType
import com.wheretogo.presentation.PathType

sealed class MapOverlay(
    open val id: String
) {
    data class MarkerContainer(
        override val id: String,
        val type: MarkerType = MarkerType.SPOT,
        val marker: Marker
    ) : MapOverlay(id)

    data class PathContainer(
        override val id: String,
        val type: PathType = PathType.FULL,
        val path: PathOverlay
    ) : MapOverlay(id)
}

