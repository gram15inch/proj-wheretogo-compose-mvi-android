package com.wheretogo.presentation.intent

import com.naver.maps.map.NaverMap
import com.wheretogo.domain.model.Journey
import com.wheretogo.domain.model.LatLng
import com.wheretogo.domain.model.Viewport

sealed class DriveScreenIntent {
    object MapIsReady : DriveScreenIntent()
    object MoveToCurrentLocation : DriveScreenIntent()
    data class UpdateCamera(val latLng: LatLng, val viewPort: Viewport) : DriveScreenIntent()
    data class UpdateLocation(val latLng: LatLng) : DriveScreenIntent()
    data class MarkerClick(val code: Int) : DriveScreenIntent()
    data class ListItemClick(val journey: Journey) : DriveScreenIntent()
    object FloatingButtonClick : DriveScreenIntent()
}