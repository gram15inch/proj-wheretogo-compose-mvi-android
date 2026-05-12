package com.wheretogo.presentation.model

import com.wheretogo.domain.ZOOM
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.map.CameraMoveTrigger
import com.wheretogo.domain.model.map.MarkerInfo
import com.wheretogo.domain.model.map.MoveAnimation
import com.wheretogo.domain.model.map.MoveCameraOption
import com.wheretogo.presentation.SEARCH_MARKER

data class SearchBarItem(
    val label: String,
    val address: String = "",
    val latlng: LatLng? = null,
    val isCourse: Boolean = false,
    val isHighlight: Boolean = false
) {

    fun toMoveCameraOption(): MoveCameraOption {
        return MoveCameraOption(
            latlng = latlng,
            zoom = ZOOM.DISTRICT.level,
            trigger = CameraMoveTrigger.SEARCH_BAR,
            animation = MoveAnimation.APP_EASING
        )
    }

    fun toMarkerInfo(): MarkerInfo {
        return MarkerInfo(
            contentId = SEARCH_MARKER,
            position = latlng
        )
    }
}