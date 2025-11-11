package com.wheretogo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheretogo.domain.RouteAttr
import com.wheretogo.domain.SearchType
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.route.RouteCategory
import com.wheretogo.domain.usecase.course.AddCourseUseCase
import com.wheretogo.domain.usecase.util.CreateRouteUseCase
import com.wheretogo.domain.usecase.util.SearchKeywordUseCase
import com.wheretogo.presentation.AppEvent
import com.wheretogo.presentation.AppScreen
import com.wheretogo.presentation.CLEAR_ADDRESS
import com.wheretogo.presentation.COURSE_NAME_MAX_LENGTH
import com.wheretogo.presentation.CameraUpdateSource
import com.wheretogo.presentation.CourseAddVisibleMode
import com.wheretogo.presentation.DriveBottomSheetContent
import com.wheretogo.presentation.MainDispatcher
import com.wheretogo.presentation.MoveAnimation
import com.wheretogo.presentation.PathType
import com.wheretogo.presentation.R
import com.wheretogo.presentation.SheetVisibleMode
import com.wheretogo.presentation.ViewModelErrorHandler
import com.wheretogo.presentation.feature.EventBus
import com.wheretogo.presentation.feature.map.CourseAddMapOverlayService
import com.wheretogo.presentation.intent.CourseAddIntent
import com.wheretogo.presentation.model.AppMarker
import com.wheretogo.presentation.model.EventMsg
import com.wheretogo.presentation.model.SearchBarItem
import com.wheretogo.presentation.state.BottomSheetState
import com.wheretogo.presentation.state.CameraState
import com.wheretogo.presentation.state.CourseAddScreenState
import com.wheretogo.presentation.state.NaverMapState
import com.wheretogo.presentation.toCourseContent
import com.wheretogo.presentation.toSearchBarItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named


@HiltViewModel
class CourseAddViewModel @Inject constructor(
    @Named("courseAddError") private val errorHandler: ViewModelErrorHandler,
    @MainDispatcher private val dispatcher: CoroutineDispatcher,
    private val createRouteUseCase: CreateRouteUseCase,
    private val addCourseUseCase: AddCourseUseCase,
    private val searchKeywordUseCase: SearchKeywordUseCase,
    private val mapOverlayService: CourseAddMapOverlayService,
) : ViewModel() {
    private val _courseAddScreenState = MutableStateFlow(
        CourseAddScreenState(
            bottomSheetState = BottomSheetState(
                content = DriveBottomSheetContent.COURSE_ADD
            )
        )
    )
    val courseAddScreenState: StateFlow<CourseAddScreenState> = _courseAddScreenState

    fun handleIntent(intent: CourseAddIntent) {
        viewModelScope.launch(dispatcher) {
            when (intent) {

                //서치바
                is CourseAddIntent.SearchBarItemClick -> searchBarItemClick(intent.searchBarItem)
                is CourseAddIntent.SearchBarClick -> searchBarClick()
                is CourseAddIntent.SearchBarClose -> searchBarClose()
                is CourseAddIntent.SubmitClick -> submitClick(intent.submitVaule)

                //지도
                is CourseAddIntent.MapClick -> mapClick(intent.latLng)
                is CourseAddIntent.CameraUpdated -> cameraUpdated(intent.cameraState)
                is CourseAddIntent.WaypointMarkerClick -> waypointMarkerClick(intent.marker)

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

    suspend fun handleError(error: Throwable) {
        errorHandler.handle(error)
    }


    //서치바
    private fun searchBarItemClick(item: SearchBarItem) {
        if (item.label != CLEAR_ADDRESS && item.latlng != null) {
            _courseAddScreenState.update {
                it.run {
                    copy(
                        searchBarState = searchBarState.copy(isLoading = false),
                        naverMapState = NaverMapState(
                            cameraState = naverMapState.cameraState.copy(
                                latLng = item.latlng,
                                updateSource = CameraUpdateSource.SEARCH_BAR,
                                moveAnimation = MoveAnimation.APP_EASING
                            )
                        )
                    )
                }
            }
        } else {
            _courseAddScreenState.update { it.searchBarInit() }
        }
    }

    private fun searchBarClick() {
        _courseAddScreenState.update {
            it.copy(
                searchBarState = it.searchBarState.copy(
                    isActive = true,
                    searchBarItemGroup = emptyList()
                ),
            )
        }
    }

    private fun searchBarClose() {
        _courseAddScreenState.update { it.searchBarInit() }
    }

    private suspend fun submitClick(submitValue: String) {
        if (submitValue.trim().isBlank()) {
            _courseAddScreenState.update { it.searchBarInit() }
            return
        }

        _courseAddScreenState.update {
            it.run { copy(searchBarState = searchBarState.copy(isLoading = true)) }
        }

        val keywordResult = withContext(Dispatchers.IO) {
            searchKeywordUseCase(
                submitValue,
                SearchType.ADDRESS
            )
        }
        keywordResult.onSuccess { addressGroup ->
            _courseAddScreenState.update {
                it.copy(
                    searchBarState = it.searchBarState.copy(
                        isLoading = false,
                        isEmptyVisible = addressGroup.isEmpty(),
                        searchBarItemGroup = addressGroup.map { it.toSearchBarItem() }
                    )
                )
            }
        }.onFailure {
            handleError(it)
        }
    }


    //지도
    private fun mapClick(latlng: LatLng) {
        _courseAddScreenState.update {
            it.run {
                if (isFloatingButton) {
                    copy()
                } else {
                    mapOverlayService.addWaypoint(latlng)
                    mapOverlayService.createScaffoldPath()
                    copy(
                        overlayGroup = mapOverlayService.overlays.toList()
                    )
                }
            }
        }
    }

    private fun cameraUpdated(cameraState: CameraState) {
        _courseAddScreenState.update {
            it.run {
                copy(
                    naverMapState = NaverMapState(
                        cameraState = cameraState.copy(
                            updateSource = CameraUpdateSource.USER
                        )
                    )
                )
            }
        }
    }

    private fun waypointMarkerClick(appMarker: AppMarker) {
        val isFloatMarker = _courseAddScreenState.value.isFloatMarker
        val position = appMarker.markerInfo.position

        if (!isFloatMarker && position != null)
            _courseAddScreenState.update {
                it.run {
                    mapOverlayService.hideWaypoint(appMarker.markerInfo.contentId)
                    copy(
                        isFloatingButton = true,
                        isFloatMarker = true,
                        selectedMarkerItem = appMarker,
                        naverMapState = NaverMapState(
                            cameraState = naverMapState.cameraState.copy(
                                latLng = position,
                                updateSource = CameraUpdateSource.MARKER
                            )
                        )
                    )
                }
            }
    }

    //플로팅
    private fun markerRemoveFloatingClick() {
        _courseAddScreenState.update {
            it.run {
                selectedMarkerItem?.markerInfo?.contentId?.let {
                    mapOverlayService.removeWaypoint(it)
                    mapOverlayService.createScaffoldPath()
                }

                copy(
                    overlayGroup = mapOverlayService.overlays.toList(),
                    bottomSheetState = bottomSheetState.copy(
                        courseAddSheetState = bottomSheetState.courseAddSheetState.copy(
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
    }

    private fun markerMoveFloatingClick() {
        val item = _courseAddScreenState.value.selectedMarkerItem
        if (item != null)
            _courseAddScreenState.update {
                it.run {
                    mapOverlayService.moveWaypoint(
                        item.markerInfo.contentId,
                        naverMapState.cameraState.latLng
                    )
                    mapOverlayService.createScaffoldPath()
                    copy(
                        overlayGroup = mapOverlayService.overlays.toList(),
                        selectedMarkerItem = null,
                        bottomSheetState = bottomSheetState.copy(
                            courseAddSheetState = bottomSheetState.courseAddSheetState.copy(
                                routeState = CourseAddScreenState.RouteState(),
                                isOneStepDone = false,
                                isNextStepButtonActive = false,
                            )
                        ),
                        isFloatingButton = !isFloatMarker,
                        isFloatMarker = !isFloatMarker,
                        naverMapState = naverMapState.copy(
                            cameraState = naverMapState.cameraState.copy(
                                latLng = naverMapState.cameraState.latLng,
                                updateSource = CameraUpdateSource.MARKER
                            )
                        )
                    )
                }
            }
    }


    //바텀시트
    private suspend fun routeCreateClick() {
        val waypoints = mapOverlayService.getWaypoints().map { it.marker.markerInfo.position!! }
        val route = withContext(Dispatchers.IO) { createRouteUseCase(waypoints) }

        route.onSuccess { route ->
            val pathResult = mapOverlayService.createFullPath(route.points).onFailure {
                handleError(it)
            }

            if (!pathResult.isSuccess) {
                return@onSuccess
            }

            _courseAddScreenState.update {
                val isOneStepDone = validateOneStep(it)
                it.copy(
                    overlayGroup = mapOverlayService.overlays.toList(),
                    bottomSheetState = it.bottomSheetState.copy(
                        courseAddSheetState = it.bottomSheetState.courseAddSheetState.copy(
                            isOneStepDone = isOneStepDone,
                            isNextStepButtonActive = isOneStepDone,
                            routeState = it.bottomSheetState.courseAddSheetState.routeState.copy(
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
        }.onFailure {
            handleError(it)
        }
    }

    private fun nameEditValueChange(text: String) {
        if (text.length <= COURSE_NAME_MAX_LENGTH) {
            _courseAddScreenState.update {
                it.run {
                    val isOneStepDone = validateOneStep(this)
                    copy(
                        bottomSheetState = bottomSheetState.copy(
                            courseAddSheetState = bottomSheetState.courseAddSheetState.copy(
                                courseName = text,
                                isOneStepDone = isOneStepDone,
                                isNextStepButtonActive = isOneStepDone
                            )
                        )
                    )
                }
            }
        }
    }

    private fun bottomSheetChange(state: SheetVisibleMode) {
        when (state) {
            SheetVisibleMode.Expand -> {
                _courseAddScreenState.update {
                    it.run {
                        copy(
                            stateMode = CourseAddVisibleMode.BottomSheetExpand,
                        )
                    }
                }
            }

            SheetVisibleMode.PartiallyExpand -> {
                _courseAddScreenState.update {
                    it.run {
                        copy(
                            stateMode = CourseAddVisibleMode.BottomSheetCollapse
                        )
                    }
                }
            }

            else -> {}
        }
    }

    private fun routeCategorySelect(item: RouteCategory) {
        _courseAddScreenState.update {
            it.run {
                val newSelectedItemGroup =
                    bottomSheetState.courseAddSheetState.selectedCategoryCodeGroup.toMutableMap()
                        .apply {
                            put(item.attr, item.code)
                        }

                copy(
                    bottomSheetState = bottomSheetState.copy(
                        courseAddSheetState = bottomSheetState.courseAddSheetState.copy(
                            selectedCategoryCodeGroup = newSelectedItemGroup,
                        )
                    ),
                )
            }
        }

        _courseAddScreenState.update {
            it.run {
                val isTwoStepDone = validateTwoStep(this)
                copy(
                    bottomSheetState = bottomSheetState.copy(
                        courseAddSheetState = bottomSheetState.courseAddSheetState.copy(
                            isTwoStepDone = isTwoStepDone,
                            isNextStepButtonActive = bottomSheetState.courseAddSheetState.isOneStepDone && isTwoStepDone
                        )
                    )
                )
            }
        }
    }

    private suspend fun commendClick() {
        val isTwoStep = _courseAddScreenState.value.bottomSheetState.courseAddSheetState.isTwoStep
        if (!isTwoStep) { // 첫째 페이지
            _courseAddScreenState.update {
                it.run {
                    copy(
                        bottomSheetState = bottomSheetState.copy(
                            courseAddSheetState = bottomSheetState.courseAddSheetState.copy(
                                isTwoStep = bottomSheetState.courseAddSheetState.isOneStepDone,
                                isNextStepButtonActive = bottomSheetState.courseAddSheetState.isTwoStepDone
                            )
                        )
                    )
                }
            }
        } else { // 둘쨰 페이지
            _courseAddScreenState.update { it.setContentLoading(true) }
            val result = _courseAddScreenState.value.run {
                val content = bottomSheetState.courseAddSheetState.toCourseContent()
                withContext(Dispatchers.IO) { addCourseUseCase(content) }
            }
            result.onSuccess {
                EventBus.send(AppEvent.SnackBar(EventMsg(R.string.course_add_done)))
                EventBus.send(AppEvent.Navigation(AppScreen.CourseAdd,AppScreen.Home))
            }.onFailure {
                handleError(it)
            }
            _courseAddScreenState.update { it.setContentLoading(false) }
        }
    }

    private fun detailBackClick() {
        _courseAddScreenState.update {
            it.run {
                copy(
                    bottomSheetState = bottomSheetState.copy(
                        courseAddSheetState = bottomSheetState.courseAddSheetState.copy(
                            isTwoStep = false,
                            isNextStepButtonActive = bottomSheetState.courseAddSheetState.isOneStepDone
                        )
                    )
                )
            }
        }
    }

    private fun validateOneStep(state: CourseAddScreenState): Boolean {
        return state.run {
            val isCourseNameValidate =
                state.bottomSheetState.courseAddSheetState.courseName.trim().length in 2..COURSE_NAME_MAX_LENGTH
            val isPathValidate =
                mapOverlayService.getWaypointPath()?.run { type == PathType.FULL } ?: false
            val isWaypointValidate = mapOverlayService.getWaypoints().size >= 2

            isCourseNameValidate && isWaypointValidate && isPathValidate
        }
    }

    private fun validateTwoStep(state: CourseAddScreenState): Boolean {
        val selectedGroup = state.bottomSheetState.courseAddSheetState.selectedCategoryCodeGroup

        if (selectedGroup.size != RouteAttr.entries.size)
            return false

        selectedGroup.forEach {
            if (it.value < 0)
                return false
        }
        return true
    }

    private fun CourseAddScreenState.setContentLoading(isLoading: Boolean): CourseAddScreenState {
        return run {
            copy(
                bottomSheetState = bottomSheetState.copy(
                    courseAddSheetState = bottomSheetState.courseAddSheetState.copy(
                        isLoading = isLoading
                    )
                )
            )
        }
    }

}