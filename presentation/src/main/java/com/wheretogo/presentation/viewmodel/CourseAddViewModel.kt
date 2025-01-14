package com.wheretogo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.PathOverlay
import com.wheretogo.domain.OverlayType
import com.wheretogo.domain.RouteDetailType
import com.wheretogo.domain.model.UseCaseResponse
import com.wheretogo.domain.model.map.Course
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.usecase.map.AddCourseUseCase
import com.wheretogo.domain.usecase.map.CreateRouteUseCase
import com.wheretogo.presentation.CameraStatus
import com.wheretogo.presentation.intent.CourseAddIntent
import com.wheretogo.presentation.model.MapOverlay
import com.wheretogo.presentation.state.CameraState
import com.wheretogo.presentation.state.CourseAddScreenState
import com.wheretogo.presentation.toDomainLatLng
import com.wheretogo.presentation.toNaver
import com.wheretogo.presentation.toRouteWaypointItemState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
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
                is CourseAddIntent.UpdatedCamera -> updatedCamara(intent.cameraState)
                is CourseAddIntent.NameEditValueChange -> nameEditValueChange(intent.text)

                is CourseAddIntent.MapClick -> mapClick(intent.latLng)
                is CourseAddIntent.CourseMarkerClick -> courseAddMarkerClick(intent.marker)
                is CourseAddIntent.MarkerRemoveFloatingClick -> markerRemoveFloatingClick()
                is CourseAddIntent.MarkerMoveFloatingClick -> markerMoveFloatingClick()
                is CourseAddIntent.RouteCreateClick -> routeCreateClick()
                is CourseAddIntent.RouteDetailItemClick -> routeDetailItemClick(intent.item)
                is CourseAddIntent.CommendClick -> commendClick()
                is CourseAddIntent.DetailBackClick -> detailBackClick()
                is CourseAddIntent.DragClick -> dragClick()
            }
        }
    }

    private fun nameEditValueChange(text: String) {
        if (text.length <= 17) {
            _courseAddScreenState.value = _courseAddScreenState.value.run {
                val isWaypointDone =
                    text.isNotEmpty() && waypoints.size >= 2 && points.isNotEmpty() && mapOverlay.path != null
                copy(
                    courseName = text,
                    isWaypointDone = isWaypointDone,
                    isCommendActive = isWaypointDone
                )
            }
        }
    }

    private fun updatedCamara(cameraState: CameraState) {
        _courseAddScreenState.value = _courseAddScreenState.value.run {
            copy(cameraState = cameraState.copy(status = CameraStatus.NONE))
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

    private fun mapClick(latlng: LatLng) {
        _courseAddScreenState.value = _courseAddScreenState.value.run {
            if (mapOverlay.markerGroup.size < 5) {
                val newMarkerGroup = mapOverlay.markerGroup + Marker().apply {
                    tag = "${latlng.latitude}${latlng.longitude}"
                    position = latlng.toNaver()
                }

                val newPath = if (newMarkerGroup.size < 2) {
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
                    waypoints = waypoints + latlng,
                    mapOverlay = newMapOverlay
                )
            } else this
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

    private fun markerRemoveFloatingClick() {
        _courseAddScreenState.value = _courseAddScreenState.value.run {
            val newMarkerGroup =
                mapOverlay.markerGroup.filter {
                    it.tag != selectedMarkerItem?.apply {
                        map = null
                    }?.tag
                }
            val newPath = if (newMarkerGroup.size < 2) {
                mapOverlay.path?.map = null
                null
            } else {
                mapOverlay.path?.apply { coords = newMarkerGroup.map { it.position } }
                    ?: PathOverlay().apply { coords = newMarkerGroup.map { it.position } }
            }
            val newWaypoints =
                if (selectedMarkerItem != null) waypoints - selectedMarkerItem.position.toDomainLatLng() else waypoints

            val newMapOverlay = MapOverlay(
                overlayId = "",
                type = OverlayType.COURSE,
                path = newPath,
                markerGroup = newMarkerGroup
            )

            copy(
                waypoints = newWaypoints,
                waypointItemStateGroup = emptyList(),
                mapOverlay = newMapOverlay,
                duration = 0,
                isFloatMarker = false,
                isFloatingButton = false,
                isWaypointDone = false,
                isCommendActive = false,
                selectedMarkerItem = null
            )
        }
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
                    waypointItemStateGroup = emptyList(),
                    mapOverlay = newMapOverlay,
                    duration = 0,
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

    private suspend fun routeCreateClick() {
        _courseAddScreenState.value = _courseAddScreenState.value.run {
            val newRoute = createRouteUseCase(waypoints) ?: return
            val naverPoints = newRoute.points.toNaver()
            val newWaypointItemState = newRoute.waypointItems.map { it.toRouteWaypointItemState() }
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
                courseName.isNotEmpty() && waypoints.size >= 2 && naverPoints.isNotEmpty() && newPath != null

            val newMapOverlay = mapOverlay.copy(
                path = newPath
            )

            copy(
                mapOverlay = newMapOverlay,
                points = newRoute.points,
                duration = newRoute.duration,
                waypointItemStateGroup = newWaypointItemState,
                isWaypointDone = isWaypointDone,
                isCommendActive = isWaypointDone
            )
        }
    }

    private suspend fun commendClick() {
        _courseAddScreenState.value = _courseAddScreenState.value.run {
            if (isDetailContent && isWaypointDone && isDetailDone) {
                val newCourse = Course(
                    courseName = courseName,
                    waypoints = waypoints,
                    points = points,
                    duration = (duration / 60000).toString(),
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
                        copy(isCourseAddDone = true)
                    }

                    else -> copy(error = "코스 등록 오류")
                }
            } else {
                copy(
                    isDetailContent = isWaypointDone,
                    isCommendActive = isDetailDone
                )
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