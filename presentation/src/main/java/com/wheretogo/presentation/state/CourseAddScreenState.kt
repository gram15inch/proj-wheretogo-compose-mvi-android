package com.wheretogo.presentation.state

import androidx.compose.ui.unit.dp
import com.wheretogo.domain.RouteAttr
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.model.map.RouteWaypointItem
import com.wheretogo.presentation.model.ContentPadding
import com.wheretogo.presentation.model.MapOverlay

data class CourseAddScreenState(
    val searchBarState: SearchBarState = SearchBarState(),
    val overlayGroup : Collection<MapOverlay> = emptyList(),
    val selectedMarkerItem: MapOverlay.MarkerContainer? = null,
    val cameraState: CameraState = CameraState(),
    val bottomSheetState: BottomSheetState = BottomSheetState(),
    val isFloatMarker: Boolean = false,
    val isFloatingButton: Boolean = false,
    val padding: ContentPadding = ContentPadding(bottom = 350.dp),
    val error: String = "",
    val timeStamp : Long =0L
) {
    data class BottomSheetState(
        val courseAddState :CourseAddState = CourseAddState(),
        val isBottomSheetDown: Boolean = false,
    )

    data class CourseAddState(
        val courseName: String = "",
        val routeState: RouteState = RouteState(),
        val selectedCategoryCodeGroup : Map<RouteAttr,Int> = emptyMap(), //카테고리 (속성, 코드)
        val isNextStepButtonActive: Boolean = false,
        val isOneStepDone: Boolean = false,
        val isTwoStep: Boolean = false,
        val isTwoStepDone: Boolean = false,
        val isLoading: Boolean = false,
    )

    data class RouteState(
        val duration: Int = 0,
        val distance: Int = 0,
        val points: List<LatLng> = emptyList(),
        val waypointItemStateGroup: List<RouteWaypointItemState> = emptyList(),
    )

    data class RouteWaypointItemState(
        val data: RouteWaypointItem = RouteWaypointItem(),
    )
}