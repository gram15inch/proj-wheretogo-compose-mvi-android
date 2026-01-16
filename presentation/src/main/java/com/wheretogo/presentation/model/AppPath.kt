package com.wheretogo.presentation.model

import com.naver.maps.map.overlay.PathOverlay
import com.wheretogo.domain.PathType
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.presentation.OverlayType
import com.wheretogo.presentation.toNaver
import com.wheretogo.presentation.toOverlayType

data class AppPath(
    override val key: String,
    override val type: OverlayType,
    val pathInfo: PathInfo,
    val corePathOverlay: PathOverlay? = null
) : MapOverlay {

    override fun getFingerPrint(): Int {
        var h = key.hashCode()
        h = 31 * h + type.hashCode()
        h = 31 * h + pathInfo.type.hashCode()
        h = 31 * h + pathInfo.contentId.hashCode()
        h = 31 * h + pathInfo.isVisible.hashCode()
        if (pathInfo.type == PathType.SCAFFOLD)
            h = 31 * h + pathInfo.points.hashCode()
        return h
    }


    override fun replaceVisible(isVisible: Boolean): AppPath {
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

    fun replaceType(type: PathType): AppPath {
        return copy(
            type = type.toOverlayType(),
            pathInfo = pathInfo.copy(type = type)
        )
    }

}
