package com.wheretogo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheretogo.domain.RouteDetailType
import com.wheretogo.domain.model.UseCaseResponse
import com.wheretogo.domain.model.map.CourseAddRequest
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.model.map.SimpleAddress
import com.wheretogo.domain.usecase.map.AddCourseUseCase
import com.wheretogo.domain.usecase.map.CreateRouteUseCase
import com.wheretogo.domain.usecase.map.GetLatLngFromAddressUseCase
import com.wheretogo.domain.usecase.map.SearchAddressUseCase
import com.wheretogo.presentation.COURSE_NAME_MAX_LENGTH
import com.wheretogo.presentation.CameraUpdateSource
import com.wheretogo.presentation.PathType
import com.wheretogo.presentation.R
import com.wheretogo.presentation.SheetState
import com.wheretogo.presentation.feature.EventBus
import com.wheretogo.presentation.feature.map.CourseAddMapOverlayService
import com.wheretogo.presentation.intent.CourseAddIntent
import com.wheretogo.presentation.model.EventMsg
import com.wheretogo.presentation.model.MapOverlay
import com.wheretogo.presentation.state.CameraState
import com.wheretogo.presentation.state.CourseAddScreenState
import com.wheretogo.presentation.toDomainLatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class CourseAddViewModel @Inject constructor(
    private val getLatLngFromAddressUseCase: GetLatLngFromAddressUseCase,
    private val createRouteUseCase: CreateRouteUseCase,
    private val addCourseUseCase: AddCourseUseCase,
    private val searchAddressUseCase: SearchAddressUseCase,
    private val mapOverlayService: CourseAddMapOverlayService
) : ViewModel() {
    private val _courseAddScreenState = MutableStateFlow(CourseAddScreenState(overlayGroup = mapOverlayService.overlays))
    val courseAddScreenState: StateFlow<CourseAddScreenState> = _courseAddScreenState

    fun handleIntent(intent: CourseAddIntent) {
        viewModelScope.launch {
            when (intent) {

                //서치바
                is CourseAddIntent.AddressItemClick -> addressItemClick(intent.simpleAddress)
                is CourseAddIntent.SearchToggleClick -> searchToggleClick(intent.isBar)
                is CourseAddIntent.SubmitClick -> submitClick(intent.submit)

                //지도
                is CourseAddIntent.MapClick -> mapClick(intent.latLng)
                is CourseAddIntent.CameraUpdated -> cameraUpdated(intent.cameraState)
                is CourseAddIntent.WaypointMarkerClick -> waypointMarkerClick(intent.marker)
                is CourseAddIntent.ContentPaddingChanged -> contentPaddingChanged(intent.amount)

                //플로팅
                is CourseAddIntent.MarkerRemoveFloatingClick -> markerRemoveFloatingClick()
                is CourseAddIntent.MarkerMoveFloatingClick -> markerMoveFloatingClick()

                //바텀시트
                is CourseAddIntent.RouteCreateClick -> routeCreateClick()
                is CourseAddIntent.NameEditValueChange -> nameEditValueChange(intent.text)
                is CourseAddIntent.SheetStateChange -> bottomSheetChange(intent.state)
                is CourseAddIntent.RouteDetailItemClick -> routeDetailItemClick(intent.item)
                is CourseAddIntent.CommendClick -> commendClick()
                is CourseAddIntent.DetailBackClick -> detailBackClick()

            }
        }
    }

    //서치바
    private suspend fun addressItemClick(simpleAddress: SimpleAddress) {
        _courseAddScreenState.value =
            _courseAddScreenState.value.run { copy(searchBarState = searchBarState.copy(isLoading = true)) }
        val latlngResponse =
            withContext(Dispatchers.IO) { getLatLngFromAddressUseCase(simpleAddress.address) }
        _courseAddScreenState.value = _courseAddScreenState.value.run {

            when (latlngResponse.status) {
                UseCaseResponse.Status.Success -> {
                    val newLatLng = latlngResponse.data!!
                    copy(
                        searchBarState = searchBarState.copy(isLoading = false),
                        cameraState = cameraState.copy(
                            latLng = newLatLng,
                            updateSource = CameraUpdateSource.APP_EASING
                        )

                    )
                }

                UseCaseResponse.Status.Fail -> {
                    copy(
                        searchBarState = searchBarState.copy(isLoading = false)
                    )
                }
            }

        }

    }

    private fun searchToggleClick(isBar: Boolean) {

        _courseAddScreenState.value = _courseAddScreenState.value.run {
            if (!isBar) {
                copy(
                    searchBarState = searchBarState.copy(
                        isLoading = false,
                        simpleAddressGroup = emptyList()
                    )
                )
            } else {
                copy()
            }
        }

    }

    private suspend fun submitClick(submit: String) {
        _courseAddScreenState.value =
            _courseAddScreenState.value.run { copy(searchBarState = searchBarState.copy(isLoading = true)) }
        val addressResponse = withContext(Dispatchers.IO) { searchAddressUseCase(submit) }
        _courseAddScreenState.value = _courseAddScreenState.value.run {
            when (addressResponse.status) {
                UseCaseResponse.Status.Success -> {
                    copy(
                        searchBarState = searchBarState.copy(
                            isLoading = false,
                            isEmptyVisible = addressResponse.data?.isEmpty() ?: false,
                            simpleAddressGroup = addressResponse.data ?: emptyList()
                        )
                    )
                }

                UseCaseResponse.Status.Fail -> {
                    copy(
                        searchBarState = searchBarState.copy(
                            isLoading = false
                        )
                    )
                }
            }

        }

    }


    //지도
    private fun mapClick(latlng: LatLng) {
        _courseAddScreenState.value = _courseAddScreenState.value.run {
            if (isFloatingButton) {
                copy()
            } else {
                mapOverlayService.addWaypoint(latlng)
                mapOverlayService.createWaypointPath()
                copy(
                    timeStamp = System.currentTimeMillis()
                )
            }
        }
    }

    private fun cameraUpdated(cameraState: CameraState) {
        _courseAddScreenState.value = _courseAddScreenState.value.run {
            copy(cameraState = cameraState.copy(updateSource = CameraUpdateSource.USER))
        }
    }

    private fun waypointMarkerClick(markerContainer : MapOverlay.MarkerContainer) {
        val isFloatMarker = _courseAddScreenState.value.isFloatMarker
        if(!isFloatMarker)
            _courseAddScreenState.value = _courseAddScreenState.value.run {
                mapOverlayService.hideWaypoint(markerContainer.id)
                copy(
                    isFloatingButton = true,
                    isFloatMarker = true,
                    selectedMarkerItem = markerContainer,
                    cameraState = cameraState.copy(
                        latLng = markerContainer.marker.position.toDomainLatLng(),
                        updateSource = CameraUpdateSource.APP_LINEAR
                    )
                )
            }
    }

    private fun contentPaddingChanged(amount:Int){}


    //플로팅
    private fun markerRemoveFloatingClick() {
        _courseAddScreenState.value = _courseAddScreenState.value.run {
            selectedMarkerItem?.id?.let{
                mapOverlayService.removeWaypoint(it)
                mapOverlayService.createWaypointPath()
            }

            copy(
                bottomSheetState = bottomSheetState.copy(
                    courseAddState = bottomSheetState.courseAddState.copy(
                        routeState = CourseAddScreenState.RouteState(),
                        isOneStepDone = false,
                        isNextStepButtonActive = false
                    )
                ),
                selectedMarkerItem = null,
                isFloatMarker = false,
                isFloatingButton = false,
            )
        }
    }

    private fun markerMoveFloatingClick() {
        val item =_courseAddScreenState.value.selectedMarkerItem
        if (item != null)
            _courseAddScreenState.value = _courseAddScreenState.value.run {
                mapOverlayService.moveWaypoint(item.id, cameraState.latLng)
                mapOverlayService.createWaypointPath()
                copy(
                    selectedMarkerItem = null,
                    bottomSheetState = bottomSheetState.copy(
                        courseAddState = bottomSheetState.courseAddState.copy(
                            routeState = CourseAddScreenState.RouteState(),
                            isOneStepDone = false,
                            isNextStepButtonActive = false,
                        )
                    ),
                    isFloatingButton = !isFloatMarker,
                    isFloatMarker = !isFloatMarker,
                    cameraState = cameraState.copy(
                        latLng = selectedMarkerItem!!.marker.position.toDomainLatLng(),
                        updateSource = CameraUpdateSource.APP_LINEAR
                    )
                )
            }
    }


    //바텀시트
    private suspend fun routeCreateClick() {
        val waypoints = mapOverlayService.getWaypoints().map { it.marker.position.toDomainLatLng() }
        val route = withContext(Dispatchers.IO) { createRouteUseCase(waypoints) }
        if(route!=null){
            val isCreatePath = mapOverlayService.createWaypointPath(route.points)
            if(isCreatePath){
                _courseAddScreenState.value = _courseAddScreenState.value.run {
                    val isOneStepDone = validateOneStep(this)
                    copy(
                        bottomSheetState = bottomSheetState.copy(
                            courseAddState = bottomSheetState.courseAddState.copy(
                                isOneStepDone = isOneStepDone,
                                isNextStepButtonActive = isOneStepDone,
                                routeState = bottomSheetState.courseAddState.routeState.copy(
                                    duration = route.duration,
                                    distance = route.distance,
                                    points = route.points,
                                    waypointItemStateGroup = route.waypointItems.map {
                                        CourseAddScreenState.RouteWaypointItemState(it)
                                    }
                                )
                            )
                        ),


                    )
                }
            } else {
                EventBus.sendMsg(EventMsg(R.string.add_marker_by_click_map))
            }
        }else{
            EventBus.sendMsg(EventMsg(R.string.route_create_error))
        }
    }

    private fun nameEditValueChange(text: String) {
        if (text.length <= COURSE_NAME_MAX_LENGTH) {
            _courseAddScreenState.value = _courseAddScreenState.value.run {
                copy(
                    bottomSheetState = bottomSheetState.copy(
                        courseAddState = bottomSheetState.courseAddState.copy(
                            courseName = text
                        )
                    )
                ).run {
                    val isOneStepDone = validateOneStep(this)
                    copy(
                        bottomSheetState = bottomSheetState.copy(
                            courseAddState = bottomSheetState.courseAddState.copy(
                                isOneStepDone = isOneStepDone,
                                isNextStepButtonActive = isOneStepDone
                            )
                        ),

                    )
                }
            }
        }
    }

    private fun bottomSheetChange(state: SheetState){
        when(state){
            SheetState.Expand ->{
                _courseAddScreenState.value = _courseAddScreenState.value.run {
                    copy(
                        bottomSheetState = bottomSheetState.copy(
                            isBottomSheetDown = false
                        )
                    )
                }
            }

            SheetState.PartiallyExpand ->{
                _courseAddScreenState.value = _courseAddScreenState.value.run {
                    copy(
                        bottomSheetState = bottomSheetState.copy(
                            isBottomSheetDown = true
                        )
                    )
                }
            }
            else->{}
        }
    }

    private fun routeDetailItemClick(item: CourseAddScreenState.RouteDetailItemState) {
        _courseAddScreenState.value = _courseAddScreenState.value.run {
            val newDetailItemGroup =  bottomSheetState.courseAddState.detailItemStateGroup.map {
                if (it.data.type == item.data.type) {
                    if (it.data.code == item.data.code) {
                        it.copy(isClick = true)
                    } else
                        it.copy(isClick = false)
                } else {
                    it
                }
            }
            copy(
                bottomSheetState = bottomSheetState.copy(
                    courseAddState = bottomSheetState.courseAddState.copy(
                        detailItemStateGroup = newDetailItemGroup,
                    )
                ),
            )
        }

        _courseAddScreenState.value = _courseAddScreenState.value.run {
            val isTwoStepDone = validateTwoStep(this)
            copy(
                bottomSheetState = bottomSheetState.copy(
                    courseAddState = bottomSheetState.courseAddState.copy(
                        isTwoStepDone= isTwoStepDone,
                        isNextStepButtonActive = bottomSheetState.courseAddState.isOneStepDone && isTwoStepDone
                    )
                ),

                )
        }
    }

    private suspend fun commendClick() {
        val isTwoStep = _courseAddScreenState.value.bottomSheetState.courseAddState.isTwoStep
        if (!isTwoStep) { // 첫째 페이지
            _courseAddScreenState.value = _courseAddScreenState.value.run {
                copy(
                    bottomSheetState = bottomSheetState.copy(
                        courseAddState = bottomSheetState.courseAddState.copy(
                            isTwoStep = bottomSheetState.courseAddState.isOneStepDone,
                            isNextStepButtonActive = bottomSheetState.courseAddState.isTwoStepDone
                        )
                    ),
                )
            }
        } else { // 둘쨰 페이지
            _courseAddScreenState.value = _courseAddScreenState.value.setContentLoading(true)
            val addCourseResponse = _courseAddScreenState.value .run {
                val courseAddState = bottomSheetState.courseAddState
                val routeState = courseAddState.routeState
                val waypoints = routeState.waypointItemStateGroup.map { it.data.latlng }
                val newCourse = CourseAddRequest(
                    courseName = courseAddState.courseName,
                    waypoints = waypoints,
                    points = routeState.points,
                    duration = (routeState.duration / 60000).toString(),
                    type = courseAddState.detailItemStateGroup.filter { it.data.type == RouteDetailType.TYPE }
                        .firstOrNull { it.isClick }?.data!!.code,
                    level = courseAddState.detailItemStateGroup.filter { it.data.type == RouteDetailType.LEVEL }
                        .firstOrNull { it.isClick }?.data!!.code,
                    relation = courseAddState.detailItemStateGroup.filter { it.data.type == RouteDetailType.RECOMMEND }
                        .firstOrNull { it.isClick }?.data!!.code,
                    cameraLatLng = waypoints.first(),
                    zoom = ""
                )
                withContext(Dispatchers.IO) { addCourseUseCase(newCourse) }
            }

            when (addCourseResponse.status) {
                UseCaseResponse.Status.Success -> {
                    EventBus.sendMsg(EventMsg(R.string.course_add_done))
                    EventBus.navigation(R.string.navi_home)
                }

                else -> {
                    EventBus.sendMsg(EventMsg(R.string.course_add_error))
                    _courseAddScreenState.value = _courseAddScreenState.value.run {
                        copy(error = "코스 등록 오류")
                    }
                }
            }
            _courseAddScreenState.value = _courseAddScreenState.value.setContentLoading(false)
        }
    }

    private fun detailBackClick() {
        _courseAddScreenState.value = _courseAddScreenState.value.run {
            copy(
                bottomSheetState = bottomSheetState.copy(
                    courseAddState = bottomSheetState.courseAddState.copy(
                        isTwoStep = false,
                        isNextStepButtonActive = bottomSheetState.courseAddState.isOneStepDone
                    )
                ),

                )
        }
    }

    @Suppress("unused")
    private fun moveCamera(latLng: LatLng, zoom:Double? = null){
        if(latLng!=LatLng()) {
            _courseAddScreenState.value = _courseAddScreenState.value.run {
                val newZoom = zoom ?: cameraState.zoom
                copy(
                    cameraState = cameraState.copy(
                        latLng = latLng,
                        zoom = newZoom,
                        updateSource = CameraUpdateSource.APP_LINEAR
                    )
                )
            }
        }
    }

    private fun validateOneStep(state: CourseAddScreenState):Boolean{
        return state.run {
            val isCourseNameValidate = state.bottomSheetState.courseAddState.courseName.trim().length in 2..  COURSE_NAME_MAX_LENGTH
            val isPathValidate = mapOverlayService.getWaypointPath()?.run { type == PathType.FULL }?:false
            val isWaypointValidate = mapOverlayService.getWaypoints().size >= 2

            isCourseNameValidate && isWaypointValidate && isPathValidate
        }
    }

    private fun validateTwoStep(state: CourseAddScreenState):Boolean{
        return state.run {
            bottomSheetState.courseAddState.detailItemStateGroup.filter { it.isClick }.map { it.data.type }.run {
                this.contains(RouteDetailType.TYPE) &&
                        this.contains(RouteDetailType.LEVEL) &&
                        this.contains(RouteDetailType.RECOMMEND)
            }
        }
    }

    private fun CourseAddScreenState.setContentLoading(isLoading:Boolean):CourseAddScreenState{
       return run {
            copy(
                bottomSheetState = bottomSheetState.copy(
                    courseAddState = bottomSheetState.courseAddState.copy(
                        isLoading = isLoading
                    )
                )
            )
        }
    }

}