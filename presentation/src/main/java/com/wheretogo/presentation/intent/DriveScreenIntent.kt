package com.wheretogo.presentation.intent

import com.wheretogo.domain.model.Journey
import com.wheretogo.domain.model.LatLng
import com.wheretogo.domain.model.MarkerTag
import com.wheretogo.domain.model.Viewport

sealed class DriveScreenIntent {
    object MapIsReady : DriveScreenIntent()
    object MoveToCurrentLocation : DriveScreenIntent()
    data class UpdateCamera(val latLng: LatLng, val viewPort: Viewport) : DriveScreenIntent()
    data class UpdateLocation(val latLng: LatLng) : DriveScreenIntent()
    data class CourseMarkerClick(val tag: MarkerTag) : DriveScreenIntent()
    data class CheckPointMarkerClick(val tag: MarkerTag) : DriveScreenIntent()
    data class ListItemClick(val journey: Journey) : DriveScreenIntent()
    object PopUpClick : DriveScreenIntent()
    object FoldFloatingButtonClick : DriveScreenIntent()
    object CommentFloatingButtonClick : DriveScreenIntent()
}