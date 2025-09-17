package com.wheretogo.presentation.intent

import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.route.RouteCategory
import com.wheretogo.presentation.SheetVisibleMode
import com.wheretogo.presentation.model.AppMarker
import com.wheretogo.presentation.model.SearchBarItem
import com.wheretogo.presentation.state.CameraState

sealed class CourseAddIntent {

    //서치바
    data class SearchBarItemClick(val searchBarItem: SearchBarItem) : CourseAddIntent()
    data object SearchBarClick : CourseAddIntent()
    data object SearchBarClose : CourseAddIntent()
    data class SubmitClick(val submitVaule: String) : CourseAddIntent()

    //지도
    data class MapClick(val latLng: LatLng) : CourseAddIntent()
    data class CameraUpdated(val cameraState: CameraState) : CourseAddIntent()
    data class WaypointMarkerClick(val marker: AppMarker) : CourseAddIntent()

    //플로팅
    data object MarkerRemoveFloatingClick : CourseAddIntent()
    data object MarkerMoveFloatingClick : CourseAddIntent()

    //바텀시트
    data object RouteCreateClick : CourseAddIntent()
    data class NameEditValueChange(val text: String) : CourseAddIntent()
    data class SheetStateChange(val state: SheetVisibleMode) : CourseAddIntent()
    data class RouteCategorySelect(val item: RouteCategory) : CourseAddIntent()
    data object CommendClick : CourseAddIntent()
    data object DetailBackClick : CourseAddIntent()
}