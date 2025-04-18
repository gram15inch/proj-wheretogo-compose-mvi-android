package com.wheretogo.presentation.intent

import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.model.map.SimpleAddress
import com.wheretogo.presentation.model.MapOverlay
import com.wheretogo.presentation.state.CameraState
import com.wheretogo.presentation.state.CourseAddScreenState

sealed class CourseAddIntent {

    //서치바
    data class AddressItemClick(val simpleAddress: SimpleAddress) : CourseAddIntent()
    data class SearchToggleClick(val isBar:Boolean) : CourseAddIntent()
    data class SubmitClick(val submit:String) : CourseAddIntent()

    data class CameraUpdated(val cameraState: CameraState) : CourseAddIntent()
    data class MapClick(val latLng: LatLng) : CourseAddIntent()
    data class WaypointMarkerClick(val marker: MapOverlay.MarkerContainer) : CourseAddIntent()
    data object MarkerRemoveFloatingClick : CourseAddIntent()
    data object MarkerMoveFloatingClick : CourseAddIntent()
    data object RouteCreateClick : CourseAddIntent()
    data object CommendClick : CourseAddIntent()
    data class RouteDetailItemClick(val item: CourseAddScreenState.RouteDetailItemState) : CourseAddIntent()
    data object DetailBackClick : CourseAddIntent()
    data object DragClick : CourseAddIntent()
    data class NameEditValueChange(val text: String) : CourseAddIntent()
    data class ContentPaddingChanged(val amount:Int) : CourseAddIntent()
}