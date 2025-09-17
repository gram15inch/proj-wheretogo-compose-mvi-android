package com.wheretogo.presentation.model

import com.naver.maps.map.overlay.PathOverlay
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.presentation.toNaver

data class AppPath(
    val pathInfo: PathInfo,
    val corePathOverlay: PathOverlay? = null
) {
    fun replaceVisible(isVisible: Boolean): AppPath {
        return copy(
            corePathOverlay = corePathOverlay?.apply {
                this.isVisible = isVisible
            },
            pathInfo = pathInfo.copy(
                isVisible = isVisible
            )
        )
    }

    fun replacePoints(points: List<LatLng>): AppPath {
        return copy(
            corePathOverlay = corePathOverlay?.apply {
                this.coords = points.toNaver()
            },
            pathInfo = pathInfo.copy(
                points = points
            )
        )
    }

    fun reflectClear() {
        corePathOverlay?.apply {
            map = null
        }
    }
}
