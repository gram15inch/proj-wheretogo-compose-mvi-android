package com.wheretogo.presentation.model

import com.naver.maps.map.overlay.Marker
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.presentation.OverlayType
import com.wheretogo.presentation.toNaver

data class AppMarker(
    override val key: String,
    override val type: OverlayType,
    val markerInfo: MarkerInfo,
    val coreMarker: Marker? = null
) : MapOverlay {

    override fun getFingerPrint(): Int {
        var h = key.hashCode()
        h = 31 * h + type.hashCode()
        h = 31 * h + markerInfo.type.hashCode()
        h = 31 * h + markerInfo.caption.hashCode()
        h = 31 * h + markerInfo.position.hashCode()
        h = 31 * h + markerInfo.isVisible.hashCode()
        h = 31 * h + markerInfo.isHighlight.hashCode()
        return h
    }

    override fun replaceVisible(isVisible: Boolean): MapOverlay {
        return copy(
            coreMarker = coreMarker?.apply {
                this.isVisible = isVisible
            },
            markerInfo = markerInfo.copy(
                isVisible = isVisible
            )
        )
    }

    override fun reflectClear() {
        coreMarker?.apply {
            map = null
        }
    }

    fun replaceCation(caption: String): AppMarker {
        return copy(
            coreMarker = coreMarker?.apply {
                captionText = caption
            },
            markerInfo = markerInfo.copy(
                caption = caption
            )
        )

    }

    fun replacePosition(latLng: LatLng): AppMarker {
        return copy(
            coreMarker = coreMarker?.apply {
                position = latLng.toNaver()
                isVisible = true
            },
            markerInfo = markerInfo.copy(
                position = latLng,
                isVisible = true
            )
        )
    }
}