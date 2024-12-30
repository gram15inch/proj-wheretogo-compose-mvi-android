package com.wheretogo.presentation.intent

import com.naver.maps.map.overlay.Marker
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.presentation.state.CameraState
import com.wheretogo.presentation.state.CourseAddScreenState

sealed class CourseAddIntent {
    data class UpdatedCamera(val cameraState: CameraState) : CourseAddIntent()
    data class MapClick(val latLng: LatLng) : CourseAddIntent()
    data class CourseAddMarkerClick(val marker: Marker) : CourseAddIntent()
    data object MarkerRemoveFloatingClick : CourseAddIntent()
    data object MarkerMoveFloatingClick : CourseAddIntent()
    data object RouteCreateClick : CourseAddIntent()
    data object CommendClick : CourseAddIntent()
    data class RouteDetailItemClick(val item: CourseAddScreenState.RouteDetailItemState) : CourseAddIntent()
    data object DetailBackClick : CourseAddIntent()
    data class NameEditValueChange(val text: String) : CourseAddIntent()
}