package com.wheretogo.presentation.model

import com.naver.maps.map.overlay.Marker
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.presentation.toNaver

data class AppMarker(
    val markerInfo: MarkerInfo,
    val coreMarker: Marker? = null
) {
    fun replaceVisible(isVisible: Boolean): AppMarker {
        return copy(
            coreMarker = coreMarker?.apply {
                this.isVisible = isVisible
            },
            markerInfo = markerInfo.copy(
                isVisible = isVisible
            )
        )
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

    fun reflectClear() {
        coreMarker?.apply {
            map = null
        }
    }
}