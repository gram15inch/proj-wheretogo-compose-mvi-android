package com.wheretogo.presentation.model

import com.wheretogo.presentation.MarkerType
import com.wheretogo.presentation.PathType

sealed class MapOverlay(
    open val contentId: String // 컨텐츠 ID
) {
    data class MarkerContainer(
        override val contentId: String,
        val type: MarkerType = MarkerType.SPOT,
        val marker: AppMarker
    ) : MapOverlay(contentId)

    data class PathContainer(
        override val contentId: String,
        val type: PathType = PathType.FULL,
        val path: AppPath
    ) : MapOverlay(contentId)
}

