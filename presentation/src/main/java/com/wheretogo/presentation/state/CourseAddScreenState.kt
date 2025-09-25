package com.wheretogo.presentation.state

import androidx.compose.ui.unit.dp
import com.wheretogo.domain.RouteAttr
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.route.RouteWaypointItem
import com.wheretogo.presentation.CourseAddVisibleMode
import com.wheretogo.presentation.model.AppMarker
import com.wheretogo.presentation.model.ContentPadding
import com.wheretogo.presentation.model.MapOverlay
import java.util.EnumSet

data class CourseAddScreenState(
    val searchBarState: SearchBarState = SearchBarState(),
    val naverMapState: NaverMapState = NaverMapState(),
    val overlayGroup: List<MapOverlay> = emptyList(),
    val selectedMarkerItem: AppMarker? = null,
    val bottomSheetState: BottomSheetState = BottomSheetState(),
    val stateMode: CourseAddVisibleMode = CourseAddVisibleMode.BottomSheetCollapse,
    val isFloatMarker: Boolean = false,
    val isFloatingButton: Boolean = false,
    val padding: ContentPadding = ContentPadding(bottom = 350.dp)
) {
    data class CourseAddSheetState(
        val courseName: String = "",
        val routeState: RouteState = RouteState(),
        val selectedCategoryCodeGroup: Map<RouteAttr, Int> = emptyMap(), //카테고리 (속성, 코드)
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

    companion object{
        val isBottomSheetVisible : EnumSet<CourseAddVisibleMode> = EnumSet.of(CourseAddVisibleMode.BottomSheetExpand)
    }

    //초기화
    fun searchBarInit(): CourseAddScreenState {
        return copy(
            searchBarState = searchBarState.copy(
                isActive = false,
                isLoading = false,
                isEmptyVisible = false,
                searchBarItemGroup = emptyList(),
                adItemGroup = emptyList()
            )
        )
    }
}