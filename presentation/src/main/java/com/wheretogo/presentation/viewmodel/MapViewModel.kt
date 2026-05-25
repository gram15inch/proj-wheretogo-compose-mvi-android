package com.wheretogo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheretogo.domain.DriveTutorialStep
import com.wheretogo.domain.MarkerType
import com.wheretogo.domain.ZOOM
import com.wheretogo.domain.feature.LocationService
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.course.Course
import com.wheretogo.domain.model.course.CourseDirectionItem
import com.wheretogo.domain.model.map.CameraMoveTrigger
import com.wheretogo.domain.model.map.CameraState
import com.wheretogo.domain.model.map.ContentOperation
import com.wheretogo.domain.model.map.MarkerInfo
import com.wheretogo.domain.model.map.MoveAnimation
import com.wheretogo.domain.model.map.MoveCameraOption
import com.wheretogo.domain.model.map.OverlayOperation
import com.wheretogo.domain.model.map.RefreshContentOption
import com.wheretogo.domain.model.map.RefreshOverlayOption
import com.wheretogo.domain.repository.MapContentRepository
import com.wheretogo.domain.usecase.app.DriveTutorialUseCase
import com.wheretogo.domain.usecase.app.ObserveSettingsUseCase
import com.wheretogo.domain.usecase.checkpoint.GetCheckpointForMarkerUseCase
import com.wheretogo.domain.usecase.course.FilterListCourseUseCase
import com.wheretogo.domain.usecase.course.GetNearByCourseUseCase
import com.wheretogo.presentation.AppLifecycle
import com.wheretogo.presentation.CHECKPOINT_ADD_MARKER
import com.wheretogo.presentation.MainDispatcher
import com.wheretogo.presentation.feature.map.MapOverlayService
import com.wheretogo.presentation.intent.MapIntent
import com.wheretogo.presentation.model.CameraOption
import com.wheretogo.presentation.state.MapState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


sealed class MapEvent{
    data class MoveCamera(val cameraState: CameraOption):MapEvent()
}

@HiltViewModel
class MapViewModel @Inject constructor(
    @MainDispatcher private val dispatcher: CoroutineDispatcher,
    private val initState: MapState,
    private val observeSettingsUseCase: ObserveSettingsUseCase,
    private val driveTutorialUseCase: DriveTutorialUseCase,
    private val getNearByCourseUseCase: GetNearByCourseUseCase,
    private val filterListCourseUseCase: FilterListCourseUseCase,
    private val getCheckPointForMarkerUseCase: GetCheckpointForMarkerUseCase,
    private val mapContentRepository: MapContentRepository,
    private val mapOverlayService: MapOverlayService,
    private val locationService: LocationService
) : ViewModel() {

    private val _state = MutableStateFlow<MapState>(initState)
    val state: StateFlow<MapState> get() = _state
    private val _event = MutableSharedFlow<MapEvent>()
    val event : SharedFlow<MapEvent> = _event

    val overlays = mapOverlayService.overlays
    val fingerPrint = mapOverlayService.fingerPrintFlow

    private var _isContentUpdate = true
    private var _isLeafScale = false

    private var _refreshJob : Job? = null
    private var latestMoveTrigger : CameraMoveTrigger? = null

    private fun overlayScope(scope: suspend ()->Unit){
        cancelOverlay()
        _refreshJob = viewModelScope.launch {
            scope()
        }
    }

    private fun cancelOverlay(){
        _refreshJob?.cancel()
    }

    init {
        observe()
    }

    private fun observe(){
        viewModelScope.launch(dispatcher){
            launch {
                mapContentRepository.selectedCourseState.collect {
                    if(it == null) {
                        _isContentUpdate = true
                        _isLeafScale = false
                        cancelOverlay()
                    }
                }
            }
            launch {
                mapContentRepository.selectedCheckPointState.collect {
                    if(it == null &&!_isContentUpdate) {
                        _isLeafScale = true
                    }else{
                        _isLeafScale = false
                    }
                }
            }
        }

    }

    fun handleIntent(intent: MapIntent) {
        viewModelScope.launch(dispatcher) {
            when (intent) {
                //지도
                is MapIntent.MapAsync -> mapAsync()
                is MapIntent.CameraUpdated -> cameraUpdated(intent.cameraState)
                is MapIntent.MarkerClick -> markerClick(intent.markerInfo)
                is MapIntent.MoveCamera -> moveCamera(intent.option)
                is MapIntent.Focus -> focus(intent.item)
                is MapIntent.RELEASE -> release()
                is MapIntent.RefreshContent -> refreshContent(intent.option)
                is MapIntent.RefreshOverlay -> refreshOverlay(intent.option)
                is MapIntent.ClearMap -> clearMap()
                is MapIntent.LifeCycleChange -> lifecycleChange(intent.lifecycle)
            }
        }
    }
    private suspend fun lifecycleChange(event: AppLifecycle) {
        when (event) {
            AppLifecycle.onLaunch -> {
                val latest = _state.value.naverMapState.latestCameraState
                if(latest.latLng!= LatLng())
                    moveToTaget(
                        latLng = latest.latLng,
                        zoom = latest.zoom,
                        trigger = CameraMoveTrigger.RESTART,
                        animation = MoveAnimation.APP_JUMP
                    )
            }
            else -> {}
        }
    }
    private fun clearMap() {
        _state.value = MapState(
            naverMapState = _state.value.naverMapState
        )
        mapOverlayService.clear()
        mapContentRepository.clear()
        initVariables()
    }

    private fun initVariables(){
        cancelOverlay()
        _isContentUpdate = true
        _isLeafScale = false
        latestMoveTrigger = null
    }

    private suspend fun focus(item: CourseDirectionItem){
        val course = item.course.cameraUpdateByDirection(item.direction)
        // 주변 코스 숨기기
        mapOverlayService.focusAndHideOthers(course.courseId)
        mapOverlayService.updateCourseMarkerPosition(course.courseId, course.cameraLatLng)
        mapContentRepository.selectCourse(item)

        moveCamera(
            MoveCameraOption(
                latlng = course.cameraLatLng,
                trigger = CameraMoveTrigger.LIST_ITEM,
                zoom = ZOOM.DISTRICT.level,
                animation = MoveAnimation.APP_LINEAR
            )
        )

        // 체크포인트 클러스터 가져오기
        refreshCheckpointCluster(course.courseId)
    }

    private fun release(){
        // 클러스터 삭제
        mapContentRepository.apply {
            selectedCourseState.value?.course?.let {
                mapOverlayService.removeCheckPointCluster(it.courseId)
            }
            clearCourse()
            clearCheckPoint()
            clearCheckPointList()
        }

        // 오버레이 전부 표시
        mapOverlayService.showAllOverlays()
    }

    private fun refreshOverlay(option: RefreshOverlayOption) {
        when (option.operation) {
            OverlayOperation.CREATE -> {
                val (info, cp) = Pair(option.markerInfo, option.checkPoint)
                when{
                    info != null -> {
                        mapOverlayService.addOneTimeMarker(listOf(info))
                    }
                    cp != null ->{
                        mapOverlayService.removeOneTimeMarker(listOf(CHECKPOINT_ADD_MARKER))
                        mapOverlayService.addCheckPointLeaf(
                            cp.courseId, cp, onLeafClick = ::checkPointLeafClick
                        )
                    }
                }
            }

            OverlayOperation.UPDATE ->{
                option.markerInfo?.let {
                    mapOverlayService.updateOneTimeMarker(it)
                }
            }

            OverlayOperation.DELETE -> {
                val (id, cs, cp) = listOf(option.markerId, option.courseId, option.checkPointId)
                when{
                    // 마커 삭제
                    id != null -> {
                        mapOverlayService.removeOneTimeMarker(listOf(id))
                    }
                    // 리프 삭제
                    cp != null && cs != null -> {
                        mapOverlayService.removeCheckPointLeaf(cs, cp)
                        mapContentRepository.clearCheckPoint()
                    }
                    // 클러스터 삭제
                    cs != null -> {
                        mapOverlayService.removeCheckPointCluster(cs)
                        mapContentRepository.clearCourse()
                    }
                    // 전체 삭제
                    else -> {
                        mapOverlayService.clear()
                        mapContentRepository.clear()
                    }
                }
            }

        }
    }

    private suspend fun refreshCheckpointCluster(courseId: String?) {
        val courseIdNotNull = courseId?.let { mapContentRepository.getIdWhenSelected(courseId) }?:courseId
        check(!courseIdNotNull.isNullOrEmpty()){ "empty courseId $courseIdNotNull"}
        _state.update { it.copy(isOverlayLoading = true) }
        withContext(Dispatchers.IO) { getCheckPointForMarkerUseCase(courseIdNotNull) }
            .onSuccess { checkPointGroup ->
                overlayScope {
                    val (hideGroup, showGroup) = checkPointGroup.partition { it.isHide }
                    mapOverlayService.removeCheckPointCluster(courseIdNotNull)
                    mapOverlayService.addCheckPointCluster(
                        courseId = courseIdNotNull,
                        checkPointGroup = showGroup,
                        onLeafRendered = {},
                        onLeafClick = ::checkPointLeafClick
                    )
                    hideGroup.forEach {
                        mapOverlayService.removeCheckPointLeaf(it.courseId, it.checkPointId)
                    }
                }
            }.onFailure {
                handleError(it)
            }
        _state.update { it.copy(isOverlayLoading = false) }
    }

    private fun deleteCourse(courseId:String?){
        val courseId = courseId?.let { mapContentRepository.getIdWhenSelected(courseId) }?:courseId
        check(!courseId.isNullOrEmpty()){ "empty courseId"}
        mapOverlayService.removeCheckPointCluster(courseId)
        mapOverlayService.removeCourseMarkerAndPath(listOf(courseId))
        mapContentRepository.clear()
    }

    private fun deleteCheckPoint(checkPointId:String?, courseId:String?){
        val checkPointId = checkPointId?.let { mapContentRepository.getIdWhenSelected(checkPointId) }?:checkPointId
        val courseId = courseId?.let { mapContentRepository.getIdWhenSelected(courseId) }?:courseId
        check(!courseId.isNullOrEmpty()){ "empty courseId"}
        check(!checkPointId.isNullOrEmpty()){ "empty checkPointId"}
        mapOverlayService.removeCheckPointLeaf(courseId = courseId, checkPointId = checkPointId)
        mapContentRepository.clearCheckPoint()
    }

    private fun handleError(e: Throwable) {

    }

    private fun mapAsync() {}

    private suspend fun cameraUpdated(cameraState: CameraState) {
        _state.update {
            it.copy(
                naverMapState = it.naverMapState.copy(
                    latestCameraState = cameraState
                )
            )
        }
        when (latestMoveTrigger) {
            CameraMoveTrigger.LIST_ITEM -> {
                _isContentUpdate = false
                _isLeafScale = true
                latestMoveTrigger = null
            }
            CameraMoveTrigger.BOTTOM_SHEET_UP->{ }
            else -> {
                val clearList = listOf(
                    CameraMoveTrigger.GUIDE,
                    CameraMoveTrigger.RESTART,
                    CameraMoveTrigger.BOTTOM_SHEET_DOWN
                )
                when{
                    _isContentUpdate -> {
                        refreshCourseByCameraState(cameraState, latestMoveTrigger)
                    }
                    _isLeafScale -> {
                        leafScaleInCluster(cameraState.latLng)
                        releaseByZoom(cameraState.zoom)
                    }
                }
                if(clearList.contains(latestMoveTrigger)){
                    latestMoveTrigger = null
                }
            }
        }
    }


    private suspend fun leafScaleInCluster(latLng: LatLng){
        val item = mapContentRepository.selectedCourseState.value
        val step = observeSettingsUseCase().firstOrNull()?.getOrNull()?.tutorialStep
            ?: DriveTutorialStep.SKIP
        if (item != null) {
            mapOverlayService.scaleToPointLeafInCluster(
                item.course.courseId,
                latLng,
            ).onSuccess { checkPointId->
                driveTutorialUseCase(DriveTutorialStep.MOVE_TO_LEAF, checkPointId)
            }
        }
    }

    private fun releaseByZoom(zoom: Double){
        if (ZOOM.CITY.areaOut(zoom)) {
            release()
        }
    }

    private suspend fun markerClick(markerInfo: MarkerInfo) {
        when (markerInfo.type) {
            MarkerType.COURSE -> courseMarkerClick(markerInfo)
            MarkerType.CHECKPOINT -> { /* 체크포인트 클릭은 클러스터 생성시 전달 */ }
            else -> {}
        }

    }

    private suspend fun courseMarkerClick(markerInfo: MarkerInfo) {
        state.value.let {
            val zoom = // 목록이 보일때 까지 확대
                maxOf(it. naverMapState.latestCameraState.zoom, ZOOM.DISTRICT.level)

            val latlng = markerInfo.position
            if (latlng == null)
                return
            moveToTaget(
                latLng = latlng,
                zoom = zoom,
                trigger = CameraMoveTrigger.MARKER,
                animation = MoveAnimation.APP_EASING
            )
        }

    }

    private suspend fun moveCamera(option: MoveCameraOption) {
        val latestCamera = _state.value.naverMapState.latestCameraState
        if(option.isMyLocation){
             moveToMyLocation(option.zoom?:latestCamera.zoom, option.trigger, option.animation)
        } else {
            val initLatlng = mapContentRepository.getLatLngWhenSelected(option.targetId)?:option.latlng
            val initZoom = option.zoom?:latestCamera.zoom

            val (finalLatlng, finalZoom) = when {
                initLatlng == null->  null to initZoom
                option.trigger == CameraMoveTrigger.LIST_ITEM -> calculateListItemCameraPosition(latestCamera, initLatlng, initZoom)
                else -> initLatlng to initZoom
            }
            moveToTaget(finalLatlng, finalZoom,option.trigger, option.animation)
        }
    }

    private fun calculateListItemCameraPosition(latestCamera: CameraState, latlng: LatLng, zoom: Double): Pair<LatLng, Double> {
        val listZoom = when {
            zoom > latestCamera.zoom -> zoom
            else -> latestCamera.zoom
        }
        val listLatLng = latlng.mapWhenHasViewport(latestCamera)
        return listLatLng to listZoom
    }

    private fun LatLng.mapWhenHasViewport(latestCamera: CameraState): LatLng {
        return this.let { latLng ->
            val hasViewPort =
                locationService.isContainByViewPort(latestCamera.viewport, latLng)
            if (hasViewPort) {
                latestCamera.latLng
            } else {
                latLng
            }
        }
    }

    private suspend fun moveToMyLocation(
        zoom: Double,
        trigger: CameraMoveTrigger = CameraMoveTrigger.DEFAULT,
        animation: MoveAnimation = MoveAnimation.APP_LINEAR
    ){
        latestMoveTrigger = trigger
        _event.emit(
            MapEvent.MoveCamera(
                CameraOption(
                    latLng = LatLng(),
                    zoom = zoom,
                    updateSource = trigger,
                    moveAnimation = animation,
                    isMyLocation = true
                )
            )
        )
    }

    private suspend fun moveToTaget(
        latLng: LatLng? = null,
        zoom: Double,
        trigger: CameraMoveTrigger = CameraMoveTrigger.DEFAULT,
        animation: MoveAnimation = MoveAnimation.APP_LINEAR
    ){
        latestMoveTrigger = trigger
        if(latLng!=null) {
            _event.emit(
                MapEvent.MoveCamera(
                    CameraOption(
                        latLng = latLng,
                        zoom = zoom,
                        updateSource = trigger,
                        moveAnimation = animation,
                        isMyLocation = false
                    )
                )
            )
        }
    }

    private suspend fun refreshContent(option: RefreshContentOption){
        runCatching {
            when(option.operation){
                ContentOperation.REFRESH_COURSES -> refreshCourseByCameraState(option.cameraState)
                ContentOperation.REFRESH_CLUSTER -> refreshCheckpointCluster(option.id)
                ContentOperation.DELETE_COURSE -> deleteCourse(option.id)
                ContentOperation.DELETE_CHECKPOINT -> deleteCheckPoint(option.id,option.groupId)
            }
        }
    }

    private suspend fun refreshCourseByCameraState(cameraState: CameraState? = null, latestMoveTrigger : CameraMoveTrigger? =null) {
        val cameraState = cameraState ?: _state.value.naverMapState.latestCameraState
        if (!_state.value.isOverlayLoading ||
            latestMoveTrigger == CameraMoveTrigger.GUIDE // 가이드가 변경한 경우 강제 리프레시
        ) {
            _state.update { it.copy(isOverlayLoading = true) }
            val courseGroup= withContext(Dispatchers.IO) {
                getNearByCourseUseCase(
                    cameraState.latLng,
                    cameraState.zoom,
                    cameraState.viewport
                )
            }.getOrDefault(emptyList())
            overlayScope {
                courseGroup
                    .refreshCourse()
                    .refreshList(cameraState)
            }
           _state.update { it.copy(isOverlayLoading = false) }
        }
    }

    private fun checkPointLeafClick(markerId: String) {
        viewModelScope.launch(dispatcher) {
            runCatching {
                val courseItem = mapContentRepository.selectedCourseState.value
                    ?: return@runCatching Exception("empty selectedCourseState")

                val checkpoints = withContext(Dispatchers.IO) {
                    getCheckPointForMarkerUseCase(courseItem.course.courseId).getOrThrow()
                }
                mapContentRepository.refreshCheckPointList(checkpoints)

                val newCheckPoint = checkpoints.first { it.checkPointId == markerId }
                mapContentRepository.selectCheckPoint(newCheckPoint)

                driveTutorialUseCase(DriveTutorialStep.LEAF_CLICK)
            }.onFailure {
                mapOverlayService.clear()
            }
        }
    }

    private fun List<Course>.refreshCourse(): List<Course> {
        return runCatching {
            val (hideGroup, showGroup) = partition { it.isHide }
            mapOverlayService.addCourseMarkerAndPath(showGroup)
            mapOverlayService.removeCourseMarkerAndPath(hideGroup.map { it.courseId })
            mapOverlayService.showAllOverlays()
            showGroup
        }.onFailure { handleError(it) }.getOrDefault(emptyList())
    }

    private suspend fun List<Course>.refreshList(cameraState: CameraState): List<Course> {
        return filterListCourseUseCase(
            cameraState.viewport,
            cameraState.zoom,
            this
        ).onSuccess { courseGroup ->
            mapContentRepository.refreshCourseList(courseGroup)
        }.onFailure { handleError(it) }.getOrDefault(emptyList())
    }
}