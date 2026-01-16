package com.wheretogo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheretogo.domain.CourseAddValid
import com.wheretogo.domain.PathType
import com.wheretogo.domain.RouteAttr
import com.wheretogo.domain.SearchType
import com.wheretogo.domain.feature.sucessMap
import com.wheretogo.domain.handler.CourseAddEvent
import com.wheretogo.domain.handler.CourseAddHandler
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.course.CourseAddValidContent
import com.wheretogo.domain.model.route.RouteCategory
import com.wheretogo.domain.usecase.course.AddCourseUseCase
import com.wheretogo.domain.usecase.util.CourseAddValidUseCase
import com.wheretogo.domain.usecase.util.CreateRouteUseCase
import com.wheretogo.domain.usecase.util.SearchKeywordUseCase
import com.wheretogo.presentation.CLEAR_ADDRESS
import com.wheretogo.presentation.COURSE_NAME_MAX_LENGTH
import com.wheretogo.presentation.CameraUpdateSource
import com.wheretogo.presentation.CourseAddVisibleMode
import com.wheretogo.presentation.DriveBottomSheetContent
import com.wheretogo.presentation.MainDispatcher
import com.wheretogo.presentation.MoveAnimation
import com.wheretogo.presentation.SheetVisibleMode
import com.wheretogo.presentation.feature.map.MapOverlayService
import com.wheretogo.presentation.intent.CourseAddIntent
import com.wheretogo.presentation.model.AppMarker
import com.wheretogo.presentation.model.AppPath
import com.wheretogo.presentation.model.MarkerInfo
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


@HiltViewModel
class CourseAddViewModel @Inject constructor(
    @MainDispatcher private val dispatcher: CoroutineDispatcher,
    private val handler: CourseAddHandler,
    private val createRouteUseCase: CreateRouteUseCase,
    private val addCourseUseCase: AddCourseUseCase,
    private val searchKeywordUseCase: SearchKeywordUseCase,
    private val courseAddValidUseCase: CourseAddValidUseCase,
    private val mapOverlayService: MapOverlayService,
) : ViewModel() {
    private val _courseAddScreenState = MutableStateFlow(
        CourseAddScreenState(
            bottomSheetState = BottomSheetState(
                content = DriveBottomSheetContent.COURSE_ADD
            ),
            overlayGroup = mapOverlayService.overlays
        )
    )
    val courseAddScreenState: StateFlow<CourseAddScreenState> = _courseAddScreenState

    init {
        observe()
    }

    private fun observe() {
        viewModelScope.launch(dispatcher) {
            launch {
                mapOverlayService.fingerPrintFlow.collect { fp ->
                    _courseAddScreenState.update { it.copy(fingerPrint = fp) }
                }
            }
        }
    }

    fun handleIntent(intent: CourseAddIntent) {
        viewModelScope.launch(dispatcher) {
            when (intent) {

                //서치바
                is CourseAddIntent.SearchBarItemClick -> searchBarItemClick(intent.searchBarItem)
                is CourseAddIntent.SearchBarClick -> searchBarClick()
                is CourseAddIntent.SearchBarClose -> searchBarClose()
                is CourseAddIntent.SubmitClick -> submitClick(intent.submitValue)

                //지도
                is CourseAddIntent.MapClick -> mapClick(intent.latLng)
                is CourseAddIntent.CameraUpdated -> cameraUpdated(intent.cameraState)
                is CourseAddIntent.WaypointMarkerClick -> waypointMarkerClick(intent.info)

                //플로팅
                is CourseAddIntent.MarkerRemoveFloatingClick -> markerRemoveFloatingClick()
                is CourseAddIntent.MarkerMoveFloatingClick -> markerMoveFloatingClick()

                //바텀시트
                is CourseAddIntent.RouteCreateClick -> routeCreateClick()
                is CourseAddIntent.CourseNameSubmit -> courseNameSubmit(intent.text)
                is CourseAddIntent.SheetStateChange -> bottomSheetChange(intent.state)
                is CourseAddIntent.RouteCategorySelect -> routeCategorySelect(intent.item)
                is CourseAddIntent.CommendClick -> commendClick()
                is CourseAddIntent.DetailBackClick -> detailBackClick()

            }
        }
    }

    suspend fun handleError(error: Throwable) {
        handler.handle(error)
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
        if (!_courseAddScreenState.value.isFloatingButton) {
            mapOverlayService.addWaypoint(latlng)
            mapOverlayService.createScaffoldPath()
        }
        _courseAddScreenState.update {
            it.run {
                copy(
                    bottomSheetState = bottomSheetState.copy(
                        courseAddSheetState = bottomSheetState.courseAddSheetState.copy(
                            pathType = PathType.SCAFFOLD
                        )
                    )
                )
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

    private fun waypointMarkerClick(info: MarkerInfo) {
        val isFloatMarker = _courseAddScreenState.value.isFloatMarker
        val position = info.position

        if (!isFloatMarker && position != null)
            _courseAddScreenState.update {
                it.run {
                    mapOverlayService.hideWaypoint(info.contentId)
                    copy(
                        isFloatingButton = true,
                        isFloatMarker = true,
                        selectedMarkerItem = info,
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
                selectedMarkerItem?.contentId?.let {
                    mapOverlayService.removeWaypoint(it)
                    mapOverlayService.createScaffoldPath()
                }

                copy(
                    bottomSheetState = bottomSheetState.copy(
                        courseAddSheetState = bottomSheetState.courseAddSheetState.copy(
                            pathType = PathType.SCAFFOLD,
                            routeState = CourseAddScreenState.RouteState(),
                            isOneStepDone = false
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
                        item.contentId,
                        naverMapState.cameraState.latLng
                    )
                    mapOverlayService.createScaffoldPath()
                    copy(
                        selectedMarkerItem = null,
                        bottomSheetState = bottomSheetState.copy(
                            courseAddSheetState = bottomSheetState.courseAddSheetState.copy(
                                pathType = PathType.SCAFFOLD,
                                routeState = CourseAddScreenState.RouteState(),
                                isOneStepDone = false,
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
        val waypoints = mapOverlayService.overlays.mapNotNull {
            if (it is AppMarker && it.markerInfo.position != null) it.markerInfo.position else null
        }

        withContext(Dispatchers.IO) {
            createRouteUseCase(waypoints).map { route ->
                _courseAddScreenState.update {
                    it.copy(
                        bottomSheetState = it.bottomSheetState.copy(
                            courseAddSheetState = it.bottomSheetState.courseAddSheetState.copy(
                                pathType = PathType.FULL,
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
                route
            }
        }.sucessMap {
            mapOverlayService.createFullPath(it.points)
        }.sucessMap {
            val content = _courseAddScreenState.value
                .bottomSheetState.courseAddSheetState.toValidContent()
            courseAddValidUseCase(content)
        }.onSuccess { denyGroup ->
            courseAddValidHandle(CourseAddValid.ROUTE, denyGroup)
            validUpdate(denyGroup)
        }.onFailure {
            handleError(it)
        }
    }

    private suspend fun courseAddValidHandle(valid: CourseAddValid, group: List<CourseAddValid>) {
        group.forEach {
            if (valid == it) when (it) {
                CourseAddValid.NAME -> handler.handle(CourseAddEvent.NAME_MIN)
                CourseAddValid.ROUTE -> handler.handle(CourseAddEvent.COURSE_CREATE_NEED)
                CourseAddValid.ATTR -> handler.handle(CourseAddEvent.WAYPOINT_MIN)
            }
        }
    }


    private fun validUpdate(group: List<CourseAddValid>) {
        val oneStep = listOf(
            CourseAddValid.NAME,
            CourseAddValid.ROUTE,
        )
        val twoStep = listOf(
            CourseAddValid.ATTR
        )

        val isOneStepDone = group.none { oneStep.contains(it) }
        val isTwoStepDone = if (isOneStepDone) group.none { twoStep.contains(it) } else false
        _courseAddScreenState.update {
            it.copy(
                bottomSheetState = it.bottomSheetState.copy(
                    courseAddSheetState = it.bottomSheetState.courseAddSheetState.copy(
                        isOneStepDone = isOneStepDone,
                        isTwoStepDone = isTwoStepDone,
                    )
                ),
            )
        }
    }

    private suspend fun courseNameSubmit(text: String) {
        _courseAddScreenState.update {
            it.run {
                copy(
                    bottomSheetState = bottomSheetState.copy(
                        courseAddSheetState = bottomSheetState.courseAddSheetState.copy(
                            courseName = text
                        )
                    )
                )
            }
        }
        val content = _courseAddScreenState.value
            .bottomSheetState
            .courseAddSheetState
            .toValidContent()
        courseAddValidUseCase(content).onSuccess { denyGroup ->
            courseAddValidHandle(CourseAddValid.NAME, denyGroup)
            validUpdate(denyGroup)
        }
    }

    private fun CourseAddScreenState.CourseAddSheetState.toValidContent(): CourseAddValidContent {
        return CourseAddValidContent(
            name = courseName,
            pathType = pathType,
            selectedAttrSize = selectedCategoryCodeGroup.size
        )
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
                        )
                    )
                )
            }
        }
    }

    private suspend fun commendClick() {
        val isCategoryStep = _courseAddScreenState.value.bottomSheetState.courseAddSheetState.isCategoryStep
        if (!isCategoryStep) { // 첫째 페이지
            _courseAddScreenState.update {
                it.run {
                    copy(
                        bottomSheetState = bottomSheetState.copy(
                            courseAddSheetState = bottomSheetState.courseAddSheetState.copy(
                                isCategoryStep = true,
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
                handler.handle(CourseAddEvent.COURSE_ADD_DONE)
                handler.handle(CourseAddEvent.HOME_NAVIGATE)
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
                            isCategoryStep = false,
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
                mapOverlayService.overlays.any { it is AppPath && it.pathInfo.type == PathType.FULL }
            val isWaypointValidate = mapOverlayService.overlays.count { it is AppMarker } >= 2

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