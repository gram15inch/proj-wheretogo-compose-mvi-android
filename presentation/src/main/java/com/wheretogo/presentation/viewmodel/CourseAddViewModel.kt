package com.wheretogo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheretogo.domain.RouteAttr

import com.wheretogo.domain.model.UseCaseResponse
import com.wheretogo.domain.model.map.CourseAddRequest
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.model.map.RouteCategory
import com.wheretogo.domain.usecase.map.AddCourseUseCase
import com.wheretogo.domain.usecase.map.CreateRouteUseCase
import com.wheretogo.domain.usecase.map.GetLatLngFromAddressUseCase
import com.wheretogo.domain.usecase.map.SearchAddressUseCase
import com.wheretogo.presentation.CLEAR_ADDRESS
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
import com.wheretogo.presentation.model.SearchBarItem
import com.wheretogo.presentation.state.CameraState
import com.wheretogo.presentation.state.CourseAddScreenState
import com.wheretogo.presentation.toDomainLatLng
import com.wheretogo.presentation.toSearchBarItem
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
                is CourseAddIntent.SearchBarItemClick -> searchBarItemClick(intent.searchBarItem)
                is CourseAddIntent.SearchBarToggleClick -> searchBarToggleClick(intent.isExpend)
                is CourseAddIntent.SubmitClick -> submitClick(intent.submitVaule)

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
                is CourseAddIntent.RouteCategorySelect -> routeCategorySelect(intent.item)
                is CourseAddIntent.CommendClick -> commendClick()
                is CourseAddIntent.DetailBackClick -> detailBackClick()

            }
        }
    }

    //서치바
    private fun searchBarItemClick(item: SearchBarItem) {
        if(item.label != CLEAR_ADDRESS && item.latlng!=null){
            _courseAddScreenState.value = _courseAddScreenState.value.run {
                copy(
                    searchBarState = searchBarState.copy(isLoading = false),
                    cameraState = cameraState.copy(
                        latLng = item.latlng,
                        updateSource = CameraUpdateSource.APP_EASING
                    )

                )
            }
        } else {
            _courseAddScreenState.value = _courseAddScreenState.value.searchBarInit()
        }
    }

    private fun searchBarToggleClick(isExpend: Boolean) {
        _courseAddScreenState.value = _courseAddScreenState.value.run {
            if (!isExpend) {
                searchBarInit()
            } else {
                this
            }
        }

    }

    private fun CourseAddScreenState.searchBarInit():CourseAddScreenState{
        return copy(
            searchBarState = searchBarState.copy(
                isLoading = false,
                isEmptyVisible = false,
                searchBarItemGroup = emptyList()
            )
        )
    }

    private suspend fun submitClick(submitValue: String) {
        if(submitValue.trim().isNotBlank()){
            _courseAddScreenState.value =
                _courseAddScreenState.value.run { copy(searchBarState = searchBarState.copy(isLoading = true)) }

            val addressResponse = withContext(Dispatchers.IO) { searchAddressUseCase(submitValue) }
            _courseAddScreenState.value = _courseAddScreenState.value.run {
                when (addressResponse.status) {
                    UseCaseResponse.Status.Success -> {
                        copy(
                            searchBarState = searchBarState.copy(
                                isLoading = false,
                                isEmptyVisible = addressResponse.data?.isEmpty() ?: false,
                                searchBarItemGroup = addressResponse.data?.map{ it.toSearchBarItem()} ?: emptyList()
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
        } else {
            _courseAddScreenState.value = _courseAddScreenState.value.searchBarInit()
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

    private fun routeCategorySelect(item: RouteCategory) {
        _courseAddScreenState.value = _courseAddScreenState.value.run {
            val newSelectedItemGroup = bottomSheetState.courseAddState.selectedCategoryCodeGroup.toMutableMap().apply {
                put(item.attr,item.code)
            }

            copy(
                bottomSheetState = bottomSheetState.copy(
                    courseAddState = bottomSheetState.courseAddState.copy(
                        selectedCategoryCodeGroup = newSelectedItemGroup,
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
                    type = courseAddState.selectedCategoryCodeGroup.get(RouteAttr.TYPE).toString(),
                    level = courseAddState.selectedCategoryCodeGroup.get(RouteAttr.LEVEL).toString(),
                    relation = courseAddState.selectedCategoryCodeGroup.get(RouteAttr.RELATION).toString(),
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
        val selectedGroup =state.bottomSheetState.courseAddState.selectedCategoryCodeGroup

        if(selectedGroup.size != RouteAttr.entries.size)
            return false

        selectedGroup.forEach {
            if(it.value<0)
                return false
        }
        return  true
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