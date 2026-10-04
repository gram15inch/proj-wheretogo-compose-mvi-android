package com.wheretogo.presentation.model

import com.naver.maps.map.overlay.PolylineOverlay
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.presentation.OverlayType
import com.wheretogo.presentation.toNaver

data class AppPolyline(
    override val key: String,
    override val type: OverlayType,
    val pathInfo: PolylineInfo,
    val corePathOverlay: PolylineOverlay? = null
) : MapOverlay {

    override fun getFingerPrint(): Int {
        var h = key.hashCode()
        h = 31 * h + type.hashCode()
        h = 31 * h + pathInfo.direction.hashCode()
        h = 31 * h + pathInfo.contentId.hashCode()
        h = 31 * h + pathInfo.isVisible.hashCode()
        h = 31 * h + pathInfo.points.size
        return h
    }


    override fun replaceVisible(isVisible: Boolean): AppPolyline {
        return copy(
            corePathOverlay = corePathOverlay?.apply {
                this.isVisible = isVisible
            },
            pathInfo = pathInfo.copy(
                isVisible = isVisible
            )
        )
    }

    override fun reflectClear() {
        corePathOverlay?.apply {
            map = null
        }
    }

    fun replacePoints(points: List<LatLng>): AppPolyline {
        return copy(
            corePathOverlay = corePathOverlay?.apply {
                this.coords = points.toNaver()
            },
            pathInfo = pathInfo.copy(
                points = points
            )
        )
    }
}
