package com.wheretogo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.overlay.PathOverlay
import com.wheretogo.domain.OverlayType
import com.wheretogo.domain.RouteDetailType
import com.wheretogo.domain.model.UseCaseResponse
import com.wheretogo.domain.model.map.Course
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.usecase.map.AddCourseUseCase
import com.wheretogo.domain.usecase.map.CreateRouteUseCase
import com.wheretogo.presentation.CameraStatus
import com.wheretogo.presentation.R
import com.wheretogo.presentation.ViewModelEvent
import com.wheretogo.presentation.intent.CourseAddIntent
import com.wheretogo.presentation.model.MapOverlay
import com.wheretogo.presentation.state.CameraState
import com.wheretogo.presentation.state.CourseAddScreenState
import com.wheretogo.presentation.toDomainLatLng
import com.wheretogo.presentation.toMarker
import com.wheretogo.presentation.toNaver
import com.wheretogo.presentation.toRouteWaypointItemState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class CourseAddViewModel @Inject constructor(
    val createRouteUseCase: CreateRouteUseCase,
    val addCourseUseCase: AddCourseUseCase,
) : ViewModel() {
    private val _courseAddScreenState = MutableStateFlow(CourseAddScreenState())
    val courseAddScreenState: StateFlow<CourseAddScreenState> = _courseAddScreenState
    private val _eventFlow = MutableSharedFlow<Pair<ViewModelEvent, Int>>()
    val eventFlow: SharedFlow<Pair<ViewModelEvent, Int>> = _eventFlow
    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        when (exception) {
            else -> {
                _courseAddScreenState.value = _courseAddScreenState.value.copy(
                    error = exception.message ?: "error"
                )
                exception.printStackTrace()
            }
        }
    }

    fun handleIntent(intent: CourseAddIntent) {
        viewModelScope.launch(exceptionHandler) {
            when (intent) {
                //지도
                is CourseAddIntent.UpdatedCamera -> updatedCamara(intent.cameraState)
                is CourseAddIntent.MapClick -> mapClick(intent.latLng)
                is CourseAddIntent.CourseMarkerClick -> courseAddMarkerClick(intent.marker)

                //플로팅
                is CourseAddIntent.MarkerRemoveFloatingClick -> markerRemoveFloatingClick()
                is CourseAddIntent.MarkerMoveFloatingClick -> markerMoveFloatingClick()

                //바텀시트
                is CourseAddIntent.RouteCreateClick -> routeCreateClick()
                is CourseAddIntent.NameEditValueChange -> nameEditValueChange(intent.text)
                is CourseAddIntent.RouteDetailItemClick -> routeDetailItemClick(intent.item)
                is CourseAddIntent.CommendClick -> commendClick()
                is CourseAddIntent.DetailBackClick -> detailBackClick()
                is CourseAddIntent.DragClick -> dragClick()
            }
        }
    }

    //지도
    private fun updatedCamara(cameraState: CameraState) {
        _courseAddScreenState.value = _courseAddScreenState.value.run {
            copy(cameraState = cameraState.copy(status = CameraStatus.NONE))
        }
    }

    private fun mapClick(latlng: LatLng) {
        _courseAddScreenState.value = _courseAddScreenState.value.run {
            if (this.isFloatingButton) {
                copy(
                    isFloatingButton = false,
                    isFloatMarker = false,
                    selectedMarkerItem = null,
                )
            } else {
                if (mapOverlay.markerGroup.size < 5) {
                    val newWaypoint = waypoints + latlng
                    val newMarkerGroup = mapOverlay.markerGroup + latlng.toMarker()
                    val newPath = if (newMarkerGroup.size < 2) {
                        null
                    } else {
                        PathOverlay().apply { coords = newMarkerGroup.map { it.position } }
                    }
                    val newMapOverlay = MapOverlay(
                        overlayId = "",
                        type = OverlayType.COURSE,
                        path = newPath,
                        markerGroup = newMarkerGroup
                    )
                    listOf(this.mapOverlay.path).removeOverlay()
                    copy(
                        waypoints = newWaypoint,
                        mapOverlay = newMapOverlay
                    )
                } else this
            }

        }
    }

    private fun courseAddMarkerClick(marker: Marker) {
        _courseAddScreenState.value = _courseAddScreenState.value.run {
            copy(
                isFloatingButton = true,
                isFloatMarker = true,
                selectedMarkerItem = marker
            )
        }
    }


    //플로팅
    private fun markerRemoveFloatingClick() {
        _courseAddScreenState.value = _courseAddScreenState.value.run {
            val newMarkerGroup = mapOverlay.markerGroup.filter { it.tag != selectedMarkerItem?.tag }
            val newPath =
                if (newMarkerGroup.size < 2) {
                    null
                } else {
                    PathOverlay().apply { coords = newMarkerGroup.map { it.position } }
                }
            val newWaypoints =
                if (selectedMarkerItem != null) waypoints - selectedMarkerItem.position.toDomainLatLng() else waypoints

            val newMapOverlay = MapOverlay(
                overlayId = "",
                type = OverlayType.COURSE,
                path = newPath,
                markerGroup = newMarkerGroup
            )
            listOf(selectedMarkerItem, this.mapOverlay.path).removeOverlay()
            copy(
                waypoints = newWaypoints,
                mapOverlay = newMapOverlay,
                selectedMarkerItem = null,
                routeState = routeState.copy(
                    duration = 0,
                    waypointItemStateGroup = emptyList()
                ),
                isFloatMarker = false,
                isFloatingButton = false,
                isWaypointDone = false,
                isCommendActive = false
            )
        }
    }

    private fun List<Overlay?>.removeOverlay() {
        forEach { it?.map = null }
    }

    private fun markerMoveFloatingClick() {
        if (_courseAddScreenState.value.selectedMarkerItem != null)
            _courseAddScreenState.value = _courseAddScreenState.value.run {
                val newMarkerGroup = mapOverlay.markerGroup.map {
                    if (it.tag == selectedMarkerItem?.tag) {
                        if (isFloatMarker) {
                            it.map = null
                            Marker().apply {
                                position = cameraState.latLng.toNaver()
                            }
                        } else {
                            it
                        }
                    } else {
                        it
                    }
                }

                val newPath = if (newMarkerGroup.size < 2) {
                    mapOverlay.path?.map = null
                    null
                } else {
                    mapOverlay.path?.apply { coords = newMarkerGroup.map { it.position } }
                        ?: PathOverlay().apply { coords = newMarkerGroup.map { it.position } }
                }
                val newMapOverlay = MapOverlay(
                    overlayId = "",
                    type = OverlayType.COURSE,
                    path = newPath,
                    markerGroup = newMarkerGroup
                )
                copy(
                    routeState = routeState.copy(
                        duration = 0,
                        waypointItemStateGroup = emptyList()
                    ),
                    mapOverlay = newMapOverlay,
                    isFloatingButton = !isFloatMarker,
                    isFloatMarker = !isFloatMarker,
                    isWaypointDone = false,
                    isCommendActive = false,
                    cameraState = cameraState.copy(
                        latLng = selectedMarkerItem!!.position.toDomainLatLng(),
                        status = if (!isFloatMarker) CameraStatus.TRACK else CameraStatus.NONE
                    )
                )
            }
    }


    //바텀시트
    private suspend fun routeCreateClick() {
        _courseAddScreenState.value = _courseAddScreenState.value.run {
            val isWaypoint = waypoints.size >= 2
            if (!isWaypoint) {
                _eventFlow.emit(ViewModelEvent.TOAST to R.string.add_marker_by_click_map)
                return
            } else {
                val newRoute = createRouteUseCase(waypoints) ?: return
                val naverPoints = newRoute.points.toNaver()
                val newWaypointItemState =
                    newRoute.waypointItems.map { it.toRouteWaypointItemState() }
                val newPath = if (mapOverlay.markerGroup.size < 2) {
                    mapOverlay.path?.map = null
                    null
                } else {
                    if (newRoute.points.size >= 2)
                        mapOverlay.path?.apply { coords = naverPoints }
                            ?: PathOverlay().apply { coords = naverPoints }
                    else
                        null
                }
                val isWaypointDone =
                    courseName.isNotEmpty() && isWaypoint && naverPoints.isNotEmpty() && newPath != null

                val newMapOverlay = mapOverlay.copy(
                    path = newPath
                )
                copy(
                    mapOverlay = newMapOverlay,
                    routeState = routeState.copy(
                        points = newRoute.points,
                        duration = newRoute.duration,
                        waypointItemStateGroup = newWaypointItemState
                    ),
                    isWaypointDone = isWaypointDone,
                    isCommendActive = isWaypointDone,
                )
            }

        }
    }

    private fun nameEditValueChange(text: String) {
        if (text.length <= 17) {
            _courseAddScreenState.value = _courseAddScreenState.value.run {
                val isWaypointDone =
                    text.isNotEmpty() && waypoints.size >= 2 && routeState.points.isNotEmpty() && mapOverlay.path != null
                copy(
                    courseName = text,
                    isWaypointDone = isWaypointDone,
                    isCommendActive = isWaypointDone
                )
            }
        }
    }

    private fun routeDetailItemClick(item: CourseAddScreenState.RouteDetailItemState) {
        _courseAddScreenState.value = _courseAddScreenState.value.run {
            val newDetailItemGroup = detailItemStateGroup.map {
                if (it.data.type == item.data.type) {
                    if (it.data.code == item.data.code) {
                        it.copy(isClick = true)
                    } else
                        it.copy(isClick = false)
                } else {
                    it
                }
            }

            val isDetailDone = newDetailItemGroup.filter { it.isClick }.map { it.data.type }.run {
                this.contains(RouteDetailType.TAG) &&
                        this.contains(RouteDetailType.LEVEL) &&
                        this.contains(RouteDetailType.RECOMMEND)
            }
            copy(
                detailItemStateGroup = newDetailItemGroup,
                isDetailDone = isDetailDone,
                isCommendActive = isDetailDone
            )
        }
    }

    private suspend fun commendClick() {
        _courseAddScreenState.value.apply {
            _courseAddScreenState.value = run {
                if (isDetailContent && isWaypointDone && isDetailDone) {
                    val newCourse = Course(
                        courseName = courseName,
                        waypoints = waypoints,
                        points = routeState.points,
                        duration = (routeState.duration / 60000).toString(),
                        tag = detailItemStateGroup.filter { it.data.type == RouteDetailType.TAG }
                            .firstOrNull() { it.isClick }?.data!!.code,
                        level = detailItemStateGroup.filter { it.data.type == RouteDetailType.LEVEL }
                            .firstOrNull() { it.isClick }?.data!!.code,
                        relation = detailItemStateGroup.filter { it.data.type == RouteDetailType.RECOMMEND }
                            .firstOrNull() { it.isClick }?.data!!.code,
                        cameraLatLng = waypoints.first(),
                        zoom = ""
                    )

                    when (addCourseUseCase(newCourse).status) {
                        UseCaseResponse.Status.Success -> {
                            _eventFlow.emit(ViewModelEvent.TOAST to R.string.course_add_done)
                            _eventFlow.emit(ViewModelEvent.NAVIGATION to R.string.navi_home)
                            copy()
                        }

                        else -> {
                            _eventFlow.emit(ViewModelEvent.TOAST to R.string.course_add_error)
                            copy(
                                error = "코스 등록 오류"
                            )
                        }
                    }
                } else {
                    copy(
                        isDetailContent = isWaypointDone,
                        isCommendActive = isDetailDone
                    )
                }
            }
        }
    }

    private fun detailBackClick() {
        _courseAddScreenState.value = _courseAddScreenState.value.run {
            copy(
                isDetailContent = false,
                isCommendActive = isWaypointDone
            )
        }
    }

    private fun dragClick() {
        _courseAddScreenState.value = _courseAddScreenState.value.run {
            copy(
                isBottomSheetDown = !isBottomSheetDown
            )
        }
    }
}