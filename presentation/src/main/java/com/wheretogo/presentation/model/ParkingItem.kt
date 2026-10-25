package com.wheretogo.presentation.model

import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.map.MarkerInfo
import com.wheretogo.presentation.PARKING_MARKER

data class ParkingItem(
    val latLng: LatLng
) {
    fun toMarkerInfo(): MarkerInfo {
        return MarkerInfo(
            contentId = PARKING_MARKER,
            position = latLng
        )
    }
}
