package com.wheretogo.presentation.intent

import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.model.map.RouteCategory
import com.wheretogo.presentation.SheetState
import com.wheretogo.presentation.model.MapOverlay
import com.wheretogo.presentation.model.SearchBarItem
import com.wheretogo.presentation.state.CameraState

sealed class CourseAddIntent {

    //서치바
    data class SearchBarItemClick(val searchBarItem: SearchBarItem) : CourseAddIntent()
    data class SearchBarToggleClick(val isExpend:Boolean) : CourseAddIntent()
    data class SubmitClick(val submitVaule:String) : CourseAddIntent()

    //지도
    data class MapClick(val latLng: LatLng) : CourseAddIntent()
    data class CameraUpdated(val cameraState: CameraState) : CourseAddIntent()
    data class WaypointMarkerClick(val marker: MapOverlay.MarkerContainer) : CourseAddIntent()
    data class ContentPaddingChanged(val amount:Int) : CourseAddIntent()

    //플로팅
    data object MarkerRemoveFloatingClick : CourseAddIntent()
    data object MarkerMoveFloatingClick : CourseAddIntent()

    //바텀시트
    data object RouteCreateClick : CourseAddIntent()
    data class NameEditValueChange(val text: String) : CourseAddIntent()
    data class SheetStateChange(val state: SheetState) : CourseAddIntent()
    data class RouteCategorySelect(val item: RouteCategory) : CourseAddIntent()
    data object CommendClick : CourseAddIntent()
    data object DetailBackClick : CourseAddIntent()
}