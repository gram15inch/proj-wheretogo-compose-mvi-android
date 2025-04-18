package com.wheretogo.presentation.state

import androidx.compose.ui.unit.dp
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.model.map.RouteDetailItem
import com.wheretogo.domain.model.map.RouteWaypointItem
import com.wheretogo.presentation.model.ContentPadding
import com.wheretogo.presentation.model.MapOverlay
import com.wheretogo.presentation.model.dummy.getRouteDetailItemGroup

data class CourseAddScreenState(
    val courseName: String = "",
    val searchBarState: SearchBarState = SearchBarState(),
    val overlayGroup : Collection<MapOverlay> = emptyList(),
    val selectedMarkerItem: MapOverlay.MarkerContainer? = null,
    val detailItemStateGroup: List<RouteDetailItemState> = getRouteDetailItemGroup()
        .map { RouteDetailItemState(data = it) },
    val routeState: RouteState = RouteState(),
    val cameraState: CameraState = CameraState(),
    val isFloatMarker: Boolean = false,
    val isFloatingButton: Boolean = false,
    val isNextStepButtonActive: Boolean = false,
    val isOneStepDone: Boolean = false,
    val isTwoStep: Boolean = false,
    val isTwoStepDone: Boolean = false,
    val isBottomSheetDown: Boolean = false,
    val isLoading: Boolean = false,
    val padding: ContentPadding = ContentPadding(bottom = 350.dp),
    val error: String = "",
    val timeStamp : Long =0L
) {

    data class RouteState(
        val duration: Int = 0,
        val distance: Int = 0,
        val points: List<LatLng> = emptyList(),
        val waypointItemStateGroup: List<RouteWaypointItemState> = emptyList(),
    )

    data class RouteDetailItemState(
        val data: RouteDetailItem = RouteDetailItem(),
        val isClick: Boolean = false
    )

    data class RouteWaypointItemState(
        val data: RouteWaypointItem = RouteWaypointItem(),
    )
}