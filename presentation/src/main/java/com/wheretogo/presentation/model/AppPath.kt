package com.wheretogo.presentation.model

import com.naver.maps.map.overlay.PathOverlay
import com.wheretogo.domain.PathType
import com.wheretogo.presentation.OverlayType

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
        h = 31 * h + pathInfo.direction.hashCode()
        h = 31 * h + pathInfo.contentId.hashCode()
        h = 31 * h + pathInfo.isVisible.hashCode()
        if (pathInfo.type == PathType.SCAFFOLD)
            h = 31 * h + pathInfo.points.hashCode()
        else
            h = 31 * h + pathInfo.points.size
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
}
