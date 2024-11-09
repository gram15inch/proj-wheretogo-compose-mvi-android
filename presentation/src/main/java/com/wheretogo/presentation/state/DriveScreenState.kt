package com.wheretogo.presentation.state

import com.wheretogo.domain.model.Journey
import com.wheretogo.presentation.model.MapOverlay


data class DriveScreenState(
    val mapState: MapState = MapState(),
    val listState: ListState = ListState(),
    val popUpState: PopUpState = PopUpState(),
    val floatingButtonState: FloatingButtonState = FloatingButtonState(),
    val error: String? = null
) {
    data class MapState(
        val isMapReady: Boolean = false,
        val mapData: List<MapOverlay> = emptyList()
    )

    data class ListState(
        val isVisible: Boolean = true,
        val listData: List<Journey> = emptyList(),
    )

    data class PopUpState(
        val isVisible: Boolean = false,
        val id: Int = -1,
        val url: String = ""
    )

    data class FloatingButtonState(
        val isVisible: Boolean = false
    )
}