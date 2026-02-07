package com.wheretogo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheretogo.domain.DriveTutorialStep
import com.wheretogo.domain.LIST_ITEM_ZOOM
import com.wheretogo.domain.MarkerType
import com.wheretogo.domain.feature.sucessMap
import com.wheretogo.domain.handler.DriveEvent
import com.wheretogo.domain.handler.DriveHandler
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.app.AppBuildConfig
import com.wheretogo.domain.model.checkpoint.CheckPoint
import com.wheretogo.domain.model.comment.Comment
import com.wheretogo.domain.model.course.Course
import com.wheretogo.domain.model.dummy.guideCheckPoint
import com.wheretogo.domain.model.dummy.guideCourse
import com.wheretogo.domain.model.util.ImageInfo
import com.wheretogo.domain.usecase.app.GuideMoveStepUseCase
import com.wheretogo.domain.usecase.app.ObserveSettingsUseCase
import com.wheretogo.domain.usecase.checkpoint.AddCheckpointToCourseUseCase
import com.wheretogo.domain.usecase.checkpoint.GetCheckpointForMarkerUseCase
import com.wheretogo.domain.usecase.checkpoint.RemoveCheckPointUseCase
import com.wheretogo.domain.usecase.checkpoint.ReportCheckPointUseCase
import com.wheretogo.domain.usecase.comment.AddCommentToCheckPointUseCase
import com.wheretogo.domain.usecase.comment.GetCommentForCheckPointUseCase
import com.wheretogo.domain.usecase.comment.RemoveCommentToCheckPointUseCase
import com.wheretogo.domain.usecase.comment.ReportCommentUseCase
import com.wheretogo.domain.usecase.course.FilterListCourseUseCase
import com.wheretogo.domain.usecase.course.GetNearByCourseUseCase
import com.wheretogo.domain.usecase.course.RemoveCourseUseCase
import com.wheretogo.domain.usecase.course.ReportCourseUseCase
import com.wheretogo.domain.usecase.user.UserSignOutUseCase
import com.wheretogo.domain.usecase.util.GetImageForPopupUseCase
import com.wheretogo.domain.usecase.util.SearchKeywordUseCase
import com.wheretogo.domain.usecase.util.UpdateLikeUseCase
import com.wheretogo.presentation.AppError
import com.wheretogo.presentation.AppEvent
import com.wheretogo.presentation.AppLifecycle
import com.wheretogo.presentation.CHECKPOINT_ADD_MARKER
import com.wheretogo.presentation.CLEAR_ADDRESS
import com.wheretogo.presentation.COURSE_DETAIL_MIN_ZOOM
import com.wheretogo.presentation.CameraUpdateSource
import com.wheretogo.presentation.CommentType
import com.wheretogo.presentation.DriveBottomSheetContent
import com.wheretogo.presentation.DriveFloatHighlight
import com.wheretogo.presentation.DriveFloatingVisibleMode
import com.wheretogo.presentation.DriveVisibleMode
import com.wheretogo.presentation.MainDispatcher
import com.wheretogo.presentation.MoveAnimation
import com.wheretogo.presentation.SEARCH_MARKER
import com.wheretogo.presentation.SheetVisibleMode
import com.wheretogo.presentation.feature.ads.AdService
import com.wheretogo.presentation.feature.map.MapOverlayService
import com.wheretogo.presentation.intent.DriveScreenIntent
import com.wheretogo.presentation.model.MarkerInfo
import com.wheretogo.presentation.model.SearchBarItem
import com.wheretogo.presentation.model.TypeEditText
import com.wheretogo.presentation.state.BottomSheetState
import com.wheretogo.presentation.state.CameraState
import com.wheretogo.presentation.state.CheckPointAddState
import com.wheretogo.presentation.state.CommentState
import com.wheretogo.presentation.state.DriveScreenState
import com.wheretogo.presentation.state.DriveScreenState.Companion.popUpVisible
import com.wheretogo.presentation.state.FloatingButtonState
import com.wheretogo.presentation.state.GuideState
import com.wheretogo.presentation.state.ListState
import com.wheretogo.presentation.state.PopUpState
import com.wheretogo.presentation.state.SearchBarState
import com.wheretogo.presentation.toAppError
import com.wheretogo.presentation.toCheckPointContent
import com.wheretogo.presentation.toCommentContent
import com.wheretogo.presentation.toCommentItemState
import com.wheretogo.presentation.toSearchBarItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.round

@HiltViewModel
class DriveViewModel @Inject constructor(
    private val stateInit: DriveScreenState,
    @MainDispatcher private val dispatcher: CoroutineDispatcher,
    private val handler: DriveHandler,
    private val observeSettingsUseCase: ObserveSettingsUseCase,
    private val getNearByCourseUseCase: GetNearByCourseUseCase,
    private val getCommentForCheckPointUseCase: GetCommentForCheckPointUseCase,
    private val getCheckPointForMarkerUseCase: GetCheckpointForMarkerUseCase,
    private val getImageForPopupUseCase: GetImageForPopupUseCase,
    private val addCheckpointToCourseUseCase: AddCheckpointToCourseUseCase,
    private val addCommentToCheckPointUseCase: AddCommentToCheckPointUseCase,
    private val updateLikeUseCase: UpdateLikeUseCase,
    private val removeCourseUseCase: RemoveCourseUseCase,
    private val removeCheckPointUseCase: RemoveCheckPointUseCase,
    private val removeCommentToCheckPointUseCase: RemoveCommentToCheckPointUseCase,
    private val reportCourseUseCase: ReportCourseUseCase,
    private val reportCheckPointUseCase: ReportCheckPointUseCase,
    private val reportCommentUseCase: ReportCommentUseCase,
    private val searchKeywordUseCase: SearchKeywordUseCase,
    private val signOutUseCase: UserSignOutUseCase,
    private val guideMoveStepUseCase: GuideMoveStepUseCase,
    private val filterListCourseUseCase: FilterListCourseUseCase,
    private val nativeAdServiceOld: AdService,
    private val mapOverlayService: MapOverlayService,
) : ViewModel() {

    private val _driveScreenState =
        MutableStateFlow(
            stateInit.copy(overlayGroup = mapOverlayService.overlays)
        )
    val driveScreenState: StateFlow<DriveScreenState> = _driveScreenState
    private var isMapUpdate = true

    fun handleIntent(intent: DriveScreenIntent) {
        viewModelScope.launch(dispatcher) {
            when (intent) {
                //가이드
                is DriveScreenIntent.GuidePopupClick -> guidePopupClick(intent.step)

                //서치바
                is DriveScreenIntent.AddressItemClick -> searchBarItemClick(intent.searchBarItem)
                is DriveScreenIntent.SearchBarClick -> searchBarClick(intent.isSkipAd)
                is DriveScreenIntent.SearchBarClose -> searchBarClose()
                is DriveScreenIntent.SearchSubmit -> searchSubmit(intent.submit)

                //지도
                is DriveScreenIntent.MapAsync -> mapAsync()
                is DriveScreenIntent.CameraUpdated -> cameraUpdated(intent.cameraState)
                is DriveScreenIntent.MarkerClick -> markerClick(intent.markerInfo)

                //목록
                is DriveScreenIntent.DriveListItemClick -> driveListItemClick(intent.itemState)

                //팝업
                is DriveScreenIntent.DismissPopupComment -> dismissCommentPopUp()
                is DriveScreenIntent.CommentListItemClick -> commentListItemClick(intent.itemState)
                is DriveScreenIntent.CommentListItemLongClick -> commentListItemLongClick(intent.comment)
                is DriveScreenIntent.CommentLikeClick -> commentLikeClick(intent.itemState)
                is DriveScreenIntent.CommentAddClick -> commentAddClick(intent.editText)
                is DriveScreenIntent.CommentRemoveClick -> commentRemoveClick(intent.comment)
                is DriveScreenIntent.CommentReportClick -> commentReportClick(intent.comment)
                is DriveScreenIntent.CommentEmogiPress -> commentEmogiPress(intent.emogi)
                is DriveScreenIntent.CommentTypePress -> commentTypePress(intent.typeEditText)

                //플로팅
                is DriveScreenIntent.CommentFloatingButtonClick -> commentFloatingButtonClick()
                is DriveScreenIntent.CheckpointAddFloatingButtonClick -> checkpointAddFloatingButtonClick()
                is DriveScreenIntent.InfoFloatingButtonClick -> infoFloatingButtonClick(intent.content)
                is DriveScreenIntent.ExportMapFloatingButtonClick -> exportMapFloatingButtonClick()
                is DriveScreenIntent.ExportMapAppButtonClick -> exportMapAppButtonClick(intent.result)
                is DriveScreenIntent.FoldFloatingButtonClick -> foldFloatingButtonClick()

                //바텀시트
                is DriveScreenIntent.BottomSheetChange -> bottomSheetChange(intent.state)
                is DriveScreenIntent.CheckpointLocationSliderChange -> checkpointLocationSliderChange(intent.percent)
                is DriveScreenIntent.CheckpointDescriptionEnterClick -> checkpointDescriptionEnterClick(intent.text)
                is DriveScreenIntent.CheckpointImageChange -> checkpointImageChange(intent.imageInfo)
                is DriveScreenIntent.CheckpointSubmitClick -> checkpointSubmitClick()
                is DriveScreenIntent.InfoReportClick -> infoReportClick(intent.reason)
                is DriveScreenIntent.InfoRemoveClick -> infoRemoveClick()

                //공통
                is DriveScreenIntent.LifecycleChange -> lifecycleChange(intent.event)
                is DriveScreenIntent.EventReceive -> eventReceive(intent.event, intent.result)
                is DriveScreenIntent.BlurClick -> blurClick()

            }
        }
    }

    fun observe() {
        viewModelScope.launch(dispatcher) {
            launch {
                observeSettingsUseCase()
                    .collect { settings ->
                        settings.onSuccess { set ->
                            setTutorialStepUi(set.tutorialStep)
                        }
                    }
            }
            launch {
                mapOverlayService.fingerPrintFlow.collect { fp ->
                    _driveScreenState.update { it.copy(fingerPrint = fp) }
                }
            }
        }
    }

    suspend fun handleError(error: Throwable) {
        when (handler.handle(error.toAppError())) {
            is AppError.InvalidState -> {
                clearScreen()
            }

            is AppError.NeedSignIn -> {
                clearAd()
                signOutUseCase()
            }

            else -> {}
        }
    }

    //서치바
    private suspend fun searchBarItemClick(item: SearchBarItem) {
        mapOverlayService.removeOneTimeMarker(listOf(SEARCH_MARKER))

        if (item.label == CLEAR_ADDRESS || item.latlng == null) {
            _driveScreenState.update { it.initSearchBar(stateInit.searchBarState.isAdVisible) }
            return
        }

        // 카메라 이동
        _driveScreenState.update {
            it.moveCamera(
                latLng = item.latlng,
                zoom = LIST_ITEM_ZOOM,
                source = CameraUpdateSource.SEARCH_BAR,
                animation = MoveAnimation.APP_EASING
            )
        }

        if (item.isCourse) {
            _driveScreenState.update {
                it.copy(
                    stateMode = DriveVisibleMode.Explorer,
                    searchBarState = it.searchBarState.copy(
                        isActive = false,
                        isLoading = false,
                        isEmptyVisible = false,
                        searchBarItemGroup = emptyList(),
                        adItemGroup = emptyList()
                    )
                )
            }
        } else {
            mapOverlayService.addOneTimeMarker(
                listOf(
                    MarkerInfo(
                        contentId = SEARCH_MARKER,
                        position = item.latlng
                    )
                )
            )
        }
        // 가이드
        val step = _driveScreenState.value.guideState.tutorialStep
        if (step == DriveTutorialStep.ADDRESS_CLICK) {
            guideMoveStepUseCase(true)
        }

    }

    private suspend fun searchBarClick(isSkipAd: Boolean) {
        if (_driveScreenState.value.stateMode == DriveVisibleMode.SearchBarExpand) {
            searchBarClose()
            return
        }

        mapOverlayService.removeOneTimeMarker(listOf(SEARCH_MARKER))
        _driveScreenState.update {
            it.run {
                copy(
                    stateMode = DriveVisibleMode.SearchBarExpand,
                    searchBarState = searchBarState.copy(
                        isActive = true,
                        searchBarItemGroup = emptyList()
                    )
                )
            }

        }


        if (!isSkipAd && _driveScreenState.value.searchBarState.adItemGroup.isEmpty()) {
            loadAd()
        }

        // 가이드
        val step = _driveScreenState.value.guideState.tutorialStep
        if (step == DriveTutorialStep.SEARCHBAR_CLICK) {
            guideMoveStepUseCase(true)
        }
    }

    private fun searchBarClose() {
        mapOverlayService.removeOneTimeMarker(listOf(SEARCH_MARKER))
        _driveScreenState.update {
            it.copy(stateMode = DriveVisibleMode.Explorer)
                .initSearchBar(stateInit.searchBarState.isAdVisible)
        }
    }

    private suspend fun searchSubmit(address: String) {
        if (address.trim().isBlank()) { // 주소 없을시 검색창 비활성화
            searchBarClose()
            return
        }

        _driveScreenState.update { it.replaceSearchBarLoading(true) }
        val keywordResult = withContext(Dispatchers.IO) { searchKeywordUseCase(address) }

        keywordResult.onSuccess { addressGroup ->
            _driveScreenState.update {
                it.copy(
                    searchBarState = it.searchBarState.copy(
                        isEmptyVisible = addressGroup.isEmpty(),
                        searchBarItemGroup = addressGroup.map { it.toSearchBarItem() }
                    )
                )
            }
            // 가이드
            val step = _driveScreenState.value.guideState.tutorialStep
            if (step == DriveTutorialStep.SEARCHBAR_EDIT) {
                guideMoveStepUseCase(true)
            }
        }.onFailure {
            handleError(it)
        }
        _driveScreenState.update { it.replaceSearchBarLoading(false) }
    }


    //지도
    private fun mapAsync() {
        observe()
    }

    private suspend fun cameraUpdated(cameraState: CameraState) {
        when (_driveScreenState.value.stateMode) {
            DriveVisibleMode.Explorer -> {
                if (isMapUpdate) {
                    isMapUpdate = false
                    _driveScreenState.update { it.replaceScreenLoading(true) }
                    refreshNearCourse(cameraState).sucessMap {
                        filterListCourseUseCase(cameraState.viewport, cameraState.zoom, it)
                    }.onSuccess { courseGroup ->
                        _driveScreenState.update { it.updateListItem(courseGroup) }
                    }.onFailure { handleError(it) }
                    _driveScreenState.update { it.replaceScreenLoading(false) }
                    isMapUpdate = true
                }

                _driveScreenState.value.apply {
                    if (guideState.tutorialStep == DriveTutorialStep.MOVE_TO_COURSE) {
                        listState.listItemGroup
                            .firstOrNull { it.course.courseId == guideCourse.courseId }
                            ?.let { guideMoveStepUseCase(true) }
                    }
                }
            }

            DriveVisibleMode.CourseDetail -> {
                val course = _driveScreenState.value.selectedCourse
                val step = _driveScreenState.value.guideState.tutorialStep
                if (course.checkpointIdGroup.isNotEmpty()) {
                    if (_driveScreenState.value.stateMode == DriveVisibleMode.CourseDetail) {
                        mapOverlayService.scaleToPointLeafInCluster(
                            course.courseId,
                            cameraState.latLng,
                        ).onSuccess {
                            if (guideCheckPoint.checkPointId == it &&
                                step == DriveTutorialStep.MOVE_TO_LEAF
                            ) {
                                guideMoveStepUseCase(true)
                            }
                        }
                    }
                }
                if (cameraState.zoom <= COURSE_DETAIL_MIN_ZOOM) {
                    foldFloatingButtonClick()
                }
            }
            else -> return
        }

        _driveScreenState.update {
            it.copy(
                naverMapState = it.naverMapState.copy(
                    latestCameraState = cameraState.copy(
                        updateSource = CameraUpdateSource.USER,
                        zoom = cameraState.zoom,
                    )
                )
            )
        }
    }

    private fun markerClick(markerInfo: MarkerInfo) {
        when (markerInfo.type) {
            MarkerType.COURSE -> courseMarkerClick(markerInfo)
            MarkerType.CHECKPOINT -> { /* 체크포인트 클릭은 클러스터 생성시 전달 아래는 테스트용 */
                checkPointLeafClick(markerInfo.contentId)
            }

            else -> {}
        }

    }

    private fun courseMarkerClick(markerInfo: MarkerInfo) {
        _driveScreenState.update {
            it.run {
                val zoom = // 목록이 보일때 까지 확대
                    maxOf(naverMapState.latestCameraState.zoom, LIST_ITEM_ZOOM + 0.1)
                val latlng = markerInfo.position
                if (latlng == null)
                    return
                it.moveCamera(
                    latLng = latlng,
                    zoom = zoom,
                    source = CameraUpdateSource.MARKER,
                    animation = MoveAnimation.APP_EASING
                )
            }
        }

    }

    private fun checkPointLeafClick(markerId: String) {
        viewModelScope.launch(dispatcher) {
            _driveScreenState.value.apply {
                if (selectedCourse.courseId.isBlank()) {
                    mapOverlayService.removeCheckPointLeaf(selectedCourse.courseId, markerId)
                    return@launch
                }
                if (DriveScreenState.Companion.bottomSheetVisible.contains(stateMode))
                    return@launch
            }
            _driveScreenState.update {
                it.run {
                    copy(
                        stateMode = DriveVisibleMode.BlurCheckpointDetail,
                        floatingButtonState = floatingButtonState.copy(
                            stateMode = DriveFloatingVisibleMode.Popup
                        ),
                    )
                }
            }
            val step = _driveScreenState.value.guideState.tutorialStep
            if (step == DriveTutorialStep.LEAF_CLICK) {
                guideMoveStepUseCase(true)
            }

            val checkpoint = withContext(Dispatchers.IO) {
                _driveScreenState.value.run {
                    val course = selectedCourse
                    getCheckPointForMarkerUseCase(course.courseId).getOrNull()
                        ?.firstOrNull { it.checkPointId == markerId }
                }
            }

            if (checkpoint == null) {
                mapOverlayService.removeOneTimeMarker(listOf(markerId))
                return@launch
            }

            _driveScreenState.update {
                it.run {
                    if (popUpVisible.contains(stateMode))
                        copy(
                            selectedCheckPoint = checkpoint
                        )
                    else this
                }

            }
            val imageUriPath =
                withContext(Dispatchers.IO) { getImageForPopupUseCase(checkpoint.imageId) }
            if (imageUriPath.isNullOrBlank())
                return@launch
            _driveScreenState.update {
                it.run {
                    if (popUpVisible.contains(stateMode))
                        copy(
                            popUpState = popUpState.copy(
                                imagePath = imageUriPath
                            )
                        )
                    else this
                }
            }
        }
    }

    private fun DriveScreenState.moveCamera(
        latLng: LatLng? = null,
        zoom: Double? = null,
        source: CameraUpdateSource = CameraUpdateSource.USER,
        animation: MoveAnimation = MoveAnimation.APP_LINEAR
    ): DriveScreenState {
        return if (latLng != null) {
            run {
                val oldZoom = zoom ?: naverMapState.latestCameraState.zoom
                val newZoom = when {
                    source == CameraUpdateSource.LIST_ITEM && oldZoom <= LIST_ITEM_ZOOM -> {
                        LIST_ITEM_ZOOM
                    }
                    else -> oldZoom
                }

                copy(
                    naverMapState = naverMapState.copy(
                        requestCameraState = naverMapState.latestCameraState.copy(
                            latLng = latLng,
                            zoom = newZoom,
                            updateSource = source,
                            moveAnimation = animation
                        )
                    )
                )
            }
        } else {
            copy(
                naverMapState = naverMapState.copy(
                    requestCameraState = naverMapState.latestCameraState.copy(
                        isMyLocation = true
                    )
                )
            )
        }
    }

    //목록
    private suspend fun driveListItemClick(listItemState: ListState.ListItemState) {
        val course = listItemState.course
        val step = _driveScreenState.value.guideState.tutorialStep
        if (step == DriveTutorialStep.DRIVE_LIST_ITEM_CLICK) {
            guideMoveStepUseCase(true)
        }
        // 코스 포커스 & 카메라 이동
        _driveScreenState.update {
            mapOverlayService.focusAndHideOthers(course)
            it.run {
                copy(
                    stateMode = DriveVisibleMode.CourseDetail,
                    isLoading = true,
                    floatingButtonState = floatingButtonState.copy(
                        stateMode = DriveFloatingVisibleMode.Default
                    ),
                    selectedCourse = course
                )
            }.moveCamera(course.cameraLatLng, source = CameraUpdateSource.LIST_ITEM)
        }

        // 체크포인트 마커 추가
        val checkPointGroup =
            withContext(Dispatchers.IO) { getCheckPointForMarkerUseCase(course.courseId) }
                .getOrDefault(emptyList())

        _driveScreenState.update {
            mapOverlayService.addCheckPointCluster(
                courseId = course.courseId,
                checkPointGroup = checkPointGroup,
                onLeafRendered = {
                    mapOverlayService.scaleToPointLeafInCluster(
                        course.courseId,
                        _driveScreenState.value.naverMapState.latestCameraState.latLng
                    )
                },
                onLeafClick = ::checkPointLeafClick
            )
            it.copy(
                isLoading = false
            )
        }
    }

    //팝업
    private fun blurClick() {
        _driveScreenState.update {
            when (it.stateMode) {
                DriveVisibleMode.BlurCheckpointBottomSheetExpand ->
                    it.copy(
                        stateMode = DriveVisibleMode.BlurCheckpointDetail,
                        floatingButtonState = it.floatingButtonState.copy(
                            stateMode = DriveFloatingVisibleMode.Popup
                        )
                    )

                DriveVisibleMode.BlurBottomSheetExpand,
                DriveVisibleMode.BlurCheckpointDetail,
                DriveVisibleMode.BlurCourseDetail -> {
                    it.backToCourseDetail()
                }

                else -> it
            }
        }
    }

    private fun dismissCommentPopUp() {
        _driveScreenState.update {
            it.copy(
                floatingButtonState = it.floatingButtonState.copy(
                    stateMode = DriveFloatingVisibleMode.Popup
                ),
                popUpState = it.popUpState.copy(
                    commentState = CommentState()
                )
            )
        }
    }

    private fun commentListItemClick(itemState: CommentState.CommentItemState) {
        val comment = itemState.data
        _driveScreenState.update {
            it.switchFold(comment.commentId)
        }
    }

    private fun commentListItemLongClick(comment: Comment) {
        val isVisible =
            _driveScreenState.value.popUpState.commentState.commentSettingState.isVisible
        _driveScreenState.update {
            it.run {
                copy(
                    popUpState = popUpState.copy(
                        commentState = popUpState.commentState.copy(
                            commentSettingState = CommentState.CommentSettingState(
                                isVisible = !isVisible,
                                comment = comment
                            )
                        ),
                    )
                )
            }

        }
    }

    private suspend fun commentLikeClick(itemState: CommentState.CommentItemState) {
        val commentId = itemState.data.commentId
        _driveScreenState.update {
            it.replaceCommentLoading(commentId, true)
                .likeSwitch(commentId)
        }

        withContext(Dispatchers.IO) {
            updateLikeUseCase(comment = itemState.data, isLike = !itemState.data.isUserLiked)
        }.onFailure { it ->
            handleError(it)

            _driveScreenState.update {
                it.replaceCommentLoading(commentId, false)
                    .likeSwitch(commentId)
            }
        }.onSuccess {
            _driveScreenState.update { it.replaceCommentLoading(commentId, false) }
        }
    }

    private suspend fun commentAddClick(editText: String) {
        val course = _driveScreenState.value.selectedCourse
        val checkpoint = _driveScreenState.value.selectedCheckPoint
        val content = _driveScreenState.value.popUpState.commentState.commentAddState
            .toCommentContent(checkpoint.checkPointId, editText)
        if (content.oneLineReview.isBlank()) {
            return
        }

        _driveScreenState.update { it.replaceCommentAddStateLoading(true) }

        withContext(Dispatchers.IO) {
            addCommentToCheckPointUseCase(content)
        }.onFailure {
            handleError(it)
        }.onSuccess { newComment ->
            _driveScreenState.updateComment(newComment, true)
            withContext(Dispatchers.IO) {
                getCheckPointForMarkerUseCase(course.courseId, listOf(newComment.groupId))
            }.onSuccess { checkPointGroup ->
                val caption =
                    checkPointGroup.firstOrNull { it.checkPointId == newComment.groupId }?.caption ?: ""
                mapOverlayService.updateCheckPointLeafCaption(
                    course.courseId,
                    content.groupId,
                    caption
                )
            }
        }
        _driveScreenState.update { it.initCommentAddState() }
    }

    private suspend fun commentRemoveClick(comment: Comment) {
        val course = _driveScreenState.value.selectedCourse
        _driveScreenState.update { it.replaceCommentSettingLoading(true) }
        val deleteResult = withContext(Dispatchers.IO) {
            removeCommentToCheckPointUseCase(
                comment.groupId,
                comment.commentId
            )
        }
        deleteResult.onSuccess {
            _driveScreenState.updateComment(comment, false)
            withContext(Dispatchers.IO) {
                getCheckPointForMarkerUseCase(course.courseId)
            }.onSuccess {
                val caption = it.firstOrNull { it.checkPointId == comment.groupId }?.caption ?: ""
                mapOverlayService.updateCheckPointLeafCaption(
                    course.courseId,
                    comment.groupId,
                    caption
                )
                _driveScreenState.update { it.replaceCommentSettingVisible(false) }
            }
        }
        _driveScreenState.update { it.replaceCommentSettingLoading(false) }
    }

    private suspend fun commentReportClick(comment: Comment) {
        val course = _driveScreenState.value.selectedCourse
        _driveScreenState.update { it.replaceCommentSettingLoading(true) }
        val result = withContext(Dispatchers.IO) { reportCommentUseCase(comment) }
        result.onSuccess {
            _driveScreenState.updateComment(comment, false)
            withContext(Dispatchers.IO) {
                getCheckPointForMarkerUseCase(course.courseId)
            }.onSuccess {
                val caption = it.firstOrNull { it.checkPointId == comment.groupId }?.caption ?: ""
                mapOverlayService.updateCheckPointLeafCaption(
                    course.courseId,
                    comment.groupId,
                    caption
                )
                _driveScreenState.update { it.replaceCommentSettingVisible(false) }
            }
        }.onFailure {
            handleError(it)
        }
        _driveScreenState.update { it.replaceCommentSettingLoading(false) }

    }

    private fun MutableStateFlow<DriveScreenState>.updateComment(comment: Comment, isAdd: Boolean) {
        val oldCommentGroup = this.value.popUpState.commentState.commentItemGroup
        val newCommentGroup = if (isAdd) {
            oldCommentGroup + comment.toCommentItemState()
        } else {
            oldCommentGroup.filter { it.data.commentId != comment.commentId }
        }
        update {
            it.run {
                copy(
                    popUpState = popUpState.copy(
                        commentState = popUpState.commentState.copy(
                            commentItemGroup = newCommentGroup
                        )
                    )
                )
            }
        }

    }

    private fun DriveScreenState.switchFold(commentId: String): DriveScreenState {
        val oldCommentGroup =
            popUpState.commentState.commentItemGroup
        val newCommentGroup =
            oldCommentGroup.map {
                if (it.data.commentId == commentId && it.data.detailedReview.isNotBlank()) {
                    it.copy(isFold = !it.isFold)
                } else {
                    it
                }
            }
        return copy(
            popUpState = popUpState.copy(
                commentState = popUpState.commentState.copy(
                    commentItemGroup = newCommentGroup,
                    commentSettingState = CommentState.CommentSettingState()
                )
            )
        )
    }

    private fun commentEmogiPress(emogi: String) {
        _driveScreenState.update {
            it.run {
                copy(
                    popUpState = popUpState.copy(
                        commentState = popUpState.commentState.copy(
                            commentAddState = popUpState.commentState.commentAddState.copy(
                                titleEmoji = emogi
                            )
                        )
                    )
                )
            }

        }
    }

    private fun commentTypePress(typeEditText: TypeEditText) {
        _driveScreenState.update {
            it.run {
                val newCommentAddState = when (typeEditText.type) {
                    CommentType.ONE -> {
                        popUpState.commentState.commentAddState.run {
                            copy(
                                commentType = typeEditText.type,
                                oneLineReview = "",
                                detailReview = typeEditText.editText,
                                isOneLinePreview = false,
                            )
                        }
                    }

                    CommentType.DETAIL -> {
                        popUpState.commentState.commentAddState.run {
                            copy(
                                commentType = typeEditText.type,
                                oneLineReview = typeEditText.editText,
                                detailReview = "",
                                isOneLinePreview = true,
                            )
                        }
                    }
                }
                copy(
                    popUpState = popUpState.copy(
                        commentState = popUpState.commentState.copy(
                            commentAddState = newCommentAddState
                        )
                    )
                )
            }

        }
    }


    // 플로팅
    private suspend fun commentFloatingButtonClick() {
        val state = _driveScreenState.value
        val checkpoint = _driveScreenState.value.selectedCheckPoint
  /*      if (state.floatingButtonState.stateMode == DriveFloatingVisibleMode.Hide) {
            _driveScreenState.update {
                it.run {
                    copy(
                        floatingButtonState = floatingButtonState.copy(
                            stateMode = DriveFloatingVisibleMode.Default
                        ),
                        popUpState = popUpState.copy(
                            commentState = CommentState()
                        )
                    )
                }
            }
            return
        }*/
        // 가이드
        val step = _driveScreenState.value.guideState.tutorialStep
        if (step == DriveTutorialStep.COMMENT_FLOAT_CLICK) {
            guideMoveStepUseCase(true)
        }

        // 코멘트 관련 Ui 표시 및 로딩 시작
        _driveScreenState.update {
            it.run {
                copy(
                    popUpState = popUpState.copy(
                        commentState = popUpState.commentState.copy(
                            isContentVisible = true,
                            isImeVisible = true
                        ),
                    ),
                    floatingButtonState = floatingButtonState.copy(
                        stateMode = DriveFloatingVisibleMode.Hide
                    ),
                ).replaceCommentListLoading(true)
            }
        }

        // 코멘트 가져오기 및 로딩 중지
        val commentResult =
            withContext(Dispatchers.IO) { getCommentForCheckPointUseCase(checkpoint.checkPointId) }
        delay(300)
        commentResult.onSuccess { commentGroup ->
            val itemGroup =
                commentGroup.map { it.toCommentItemState() }
            _driveScreenState.update {
                it.run {
                    if (popUpState.commentState.isContentVisible)
                        copy(
                            popUpState = popUpState.copy(
                                commentState = popUpState.commentState.copy(
                                    isLoading = false,
                                    commentItemGroup = itemGroup
                                )
                            )
                        )
                    else this
                }
            }
        }
    }

    private fun checkpointAddFloatingButtonClick() {
        val course = _driveScreenState.value.selectedCourse
        mapOverlayService.addOneTimeMarker(
            listOf(
                MarkerInfo(
                    contentId = CHECKPOINT_ADD_MARKER,
                    position = course.points.first()
                )
            ),
        )
        _driveScreenState.update {
            it.copy(
                stateMode = DriveVisibleMode.BottomSheetExpand,
                bottomSheetState = it.bottomSheetState.copy(
                    content = DriveBottomSheetContent.CHECKPOINT_ADD
                )
            ).initCheckPointAddState()
        }
    }

    private fun infoFloatingButtonClick(content: DriveBottomSheetContent) {
        _driveScreenState.update {
            val mode =  when(content){
                DriveBottomSheetContent.CHECKPOINT_INFO-> DriveVisibleMode.BlurCheckpointBottomSheetExpand
                else-> DriveVisibleMode.BlurBottomSheetExpand
            }
            it.run {
                copy(
                    stateMode = mode,
                    bottomSheetState = bottomSheetState.copy(
                        content = content,
                    ),
                    popUpState = popUpState.copy(
                        commentState = popUpState.commentState.copy(
                            isContentVisible = false
                        )
                    ),
                    floatingButtonState = floatingButtonState.copy(
                        stateMode = DriveFloatingVisibleMode.Hide
                    ),
                ).initInfoState()
            }
        }
    }

    private suspend fun exportMapFloatingButtonClick() {
        val floatingMode = _driveScreenState.value.floatingButtonState.stateMode
        if (floatingMode == DriveFloatingVisibleMode.ExportExpand) {
            _driveScreenState.update {
                it.copy(
                    stateMode = DriveVisibleMode.CourseDetail,
                    floatingButtonState = it.floatingButtonState.copy(
                        stateMode = DriveFloatingVisibleMode.Default,
                        adItemGroup = emptyList()
                    )
                )
            }
        } else {
            _driveScreenState.update {
                it.run {
                    copy(
                        stateMode = DriveVisibleMode.BlurCourseDetail,
                        floatingButtonState = floatingButtonState.copy(
                            stateMode = DriveFloatingVisibleMode.ExportExpand
                        )
                    )
                }
            }

            viewModelScope.launch(Dispatchers.IO) {
                delay(50)
                loadAd()
            }

            // 가이드
            val step = _driveScreenState.value.guideState.tutorialStep
            if (step == DriveTutorialStep.EXPORT_FLOAT_CLICK) {
                guideMoveStepUseCase(true)
            }
        }
    }

    private suspend fun exportMapAppButtonClick(result: Result<Unit>) {
        result.onFailure {
            handleError(it)
        }
    }

    private suspend fun foldFloatingButtonClick() {
        val course = _driveScreenState.value.selectedCourse
        _driveScreenState.update {
            mapOverlayService.removeCheckPointCluster(course.courseId)
            mapOverlayService.showAllOverlays()
            it.backToExplorer()
        }

        // 가이드
        val step = _driveScreenState.value.guideState.tutorialStep
        if (step == DriveTutorialStep.FOLD_FLOAT_CLICK) {
            guideMoveStepUseCase(true)
        }
    }


    // 바텀 시트
    private suspend fun bottomSheetChange(state: SheetVisibleMode) {
        val content = _driveScreenState.value.bottomSheetState.content
        val course = _driveScreenState.value.selectedCourse
        when (state) {
            SheetVisibleMode.Opening -> {}

            SheetVisibleMode.Opened -> {
                when (content) {
                    DriveBottomSheetContent.CHECKPOINT_ADD -> {
                        _driveScreenState.update {
                            it.moveCamera(
                                course.cameraLatLng,
                                source = CameraUpdateSource.BOTTOM_SHEET_UP
                            )
                        }
                    }

                    else -> {}
                }
            }
            SheetVisibleMode.Closing -> {
                if(_driveScreenState.value.popUpState.commentState.isContentVisible){
                    _driveScreenState.update {
                        it.copy(
                            popUpState = it.popUpState.copy(
                                commentState = it.popUpState.commentState.copy(
                                    isImeVisible = false
                                )
                            )
                        )
                    }
                }
                when (content) {
                    DriveBottomSheetContent.COURSE_INFO -> {
                        _driveScreenState.update {
                            it.copy(
                                stateMode = DriveVisibleMode.CourseDetail,
                                floatingButtonState = it.floatingButtonState.copy(
                                    stateMode = DriveFloatingVisibleMode.Default
                                )
                            )
                        }
                    }

                    DriveBottomSheetContent.CHECKPOINT_INFO -> {
                        _driveScreenState.update {
                            it.copy(
                                stateMode = DriveVisibleMode.BlurCheckpointDetail,
                                floatingButtonState = it.floatingButtonState.copy(
                                    stateMode = DriveFloatingVisibleMode.Popup
                                )
                            )
                        }

                    }

                    else -> {}
                }
            }

            SheetVisibleMode.Closed -> {
                if(_driveScreenState.value.popUpState.commentState.isContentVisible){
                    _driveScreenState.update {
                        it.copy(
                            stateMode = DriveVisibleMode.BlurCheckpointDetail,
                            floatingButtonState = it.floatingButtonState.copy(
                                stateMode = DriveFloatingVisibleMode.Popup
                            ),
                            popUpState = it.popUpState.copy(
                                commentState = CommentState()
                            )
                        )
                    }
                }
                when (content) {
                    DriveBottomSheetContent.CHECKPOINT_ADD -> {
                        mapOverlayService.removeOneTimeMarker(listOf(CHECKPOINT_ADD_MARKER))
                        _driveScreenState.update {
                            it.moveCamera(
                                course.cameraLatLng,
                                source = CameraUpdateSource.BOTTOM_SHEET_DOWN,
                            ).copy(
                                stateMode = DriveVisibleMode.CourseDetail,
                                bottomSheetState = BottomSheetState(),
                                floatingButtonState = it.floatingButtonState.copy(
                                    stateMode = DriveFloatingVisibleMode.Default
                                )
                            )
                        }
                    }

                    else -> {
                        _driveScreenState.update {
                            it.copy(
                                bottomSheetState = BottomSheetState()
                            )
                        }
                    }
                }

                val step = _driveScreenState.value.guideState.tutorialStep
                if (step == DriveTutorialStep.COMMENT_SHEET_DRAG) {
                    guideMoveStepUseCase(true)
                }
            }
        }
    }

    private suspend fun checkpointLocationSliderChange(percent: Float) {
        runCatching {
            val selectedCourse = _driveScreenState.value.selectedCourse
            val points = selectedCourse.points
            val index = when (percent) {
                100.0f -> points.size - 1
                0.0f -> 0
                else -> round((points.size - 1) * percent).toInt()
            }
            points[index]
        }.onSuccess { newLatlng ->
            _driveScreenState.update {
                mapOverlayService.updateOneTimeMarker(
                    MarkerInfo(
                        contentId = CHECKPOINT_ADD_MARKER,
                        type = MarkerType.DEFAULT,
                        position = newLatlng
                    )
                )
                val isValidateAddCheckPoint =
                    it.bottomSheetState.checkPointAddState.isValidateAddCheckPoint().isSuccess
                it.copy(
                    bottomSheetState = it.bottomSheetState.copy(
                        checkPointAddState = it.bottomSheetState.checkPointAddState.copy(
                            latLng = newLatlng,
                            sliderPercent = percent,
                            isSubmitActive = isValidateAddCheckPoint
                        )
                    )
                )
            }
        }.onFailure {
            handleError(it)
        }
    }

    private fun checkpointDescriptionEnterClick(text: String) {
        _driveScreenState.update {
            val isValidateAddCheckPoint =
                it.bottomSheetState.checkPointAddState.isValidateAddCheckPoint().isSuccess
            it.copy(
                bottomSheetState = it.bottomSheetState.copy(
                    checkPointAddState = it.bottomSheetState.checkPointAddState.copy(
                        description = text,
                        isSubmitActive = isValidateAddCheckPoint
                    )
                )
            )
        }
    }

    private fun checkpointImageChange(imageInfo: ImageInfo) {
        _driveScreenState.update {
            val isValidateAddCheckPoint =
                it.bottomSheetState.checkPointAddState.isValidateAddCheckPoint().isSuccess
            it.copy(
                bottomSheetState = it.bottomSheetState.copy(
                    checkPointAddState = it.bottomSheetState.checkPointAddState.copy(
                        imgUriString = imageInfo.uriString,
                        imgInfo = imageInfo,
                        isSubmitActive = isValidateAddCheckPoint
                    )
                )
            )
        }
    }

    private suspend fun checkpointSubmitClick() {
        val course = _driveScreenState.value.selectedCourse

        if (!_driveScreenState.value.bottomSheetState.checkPointAddState.isSubmitActive)
            return

        _driveScreenState.update { it.replaceCheckpointAddLoading(true) }
        val content =
            _driveScreenState.value.bottomSheetState.checkPointAddState
                .toCheckPointContent(course.courseId)
        val result =
            withContext(Dispatchers.IO) { addCheckpointToCourseUseCase(content) }

        result.onSuccess { newCheckPoint ->
            handler.handle(DriveEvent.ADD_DONE)
            val newCheckpointIdGroup = course.checkpointIdGroup + newCheckPoint.checkPointId
            _driveScreenState.update {
                it.run {
                    mapOverlayService.removeOneTimeMarker(listOf(CHECKPOINT_ADD_MARKER))
                    mapOverlayService.addCheckPointLeaf(
                        courseId = course.courseId,
                        checkPoint = newCheckPoint,
                        onLeafClick = ::checkPointLeafClick
                    )
                    copy(
                        stateMode = DriveVisibleMode.CourseDetail,
                        selectedCourse = course.copy(
                            checkpointIdGroup = newCheckpointIdGroup
                        ),
                        floatingButtonState = floatingButtonState.copy(
                            stateMode = DriveFloatingVisibleMode.Default
                        )
                    )
                }
            }
        }.onFailure {
            handleError(it)
            _driveScreenState.update { it.replaceCheckpointAddLoading(false) }
        }
    }

    private suspend fun infoRemoveClick() {
        val content = _driveScreenState.value.bottomSheetState.content

        _driveScreenState.update { it.replaceInfoLoading(true) }
        when (content) {
            DriveBottomSheetContent.COURSE_INFO -> {
                val course = _driveScreenState.value.selectedCourse
                val camera = _driveScreenState.value.naverMapState.latestCameraState
                withContext(Dispatchers.IO) {
                    removeCourseUseCase(course.courseId)
                }.onSuccess {
                    handler.handle(DriveEvent.REMOVE_DONE)
                    _driveScreenState.update {
                        it.clearCourseInfo()
                    }
                }.sucessMap {
                    refreshNearCourse(camera).sucessMap {
                        filterListCourseUseCase(camera.viewport, camera.zoom, it)
                    }.onSuccess { courseGroup ->
                        _driveScreenState.update { it.updateListItem(courseGroup) }
                    }
                }.onFailure {
                    handleError(it)
                }
            }

            DriveBottomSheetContent.CHECKPOINT_INFO -> {
                val checkPoint = _driveScreenState.value.selectedCheckPoint
                withContext(Dispatchers.IO) {
                    removeCheckPointUseCase(checkPoint.courseId, checkPoint.checkPointId)
                }.onSuccess {
                    handler.handle(DriveEvent.REMOVE_DONE)
                    _driveScreenState.update { it.clearCheckPointInfo() }
                }.onFailure {
                    handleError(it)
                }
            }

            else -> {}
        }
        _driveScreenState.update { it.replaceInfoLoading(false) }
    }

    private suspend fun infoReportClick(reason: String) {
        val content = _driveScreenState.value.bottomSheetState.content
        val camera = _driveScreenState.value.naverMapState.latestCameraState
        _driveScreenState.update { it.replaceInfoLoading(true) }

        when (content) {
            DriveBottomSheetContent.COURSE_INFO -> {
                val course = _driveScreenState.value.selectedCourse
                withContext(Dispatchers.IO) {
                    reportCourseUseCase(course, reason).onSuccess {
                        handler.handle(DriveEvent.REPORT_DONE)
                        _driveScreenState.update {
                            it.clearCourseInfo()
                        }
                    }
                }.sucessMap {
                    refreshNearCourse(camera).sucessMap {
                        filterListCourseUseCase(camera.viewport, camera.zoom, it)
                    }.onSuccess { courseGroup ->
                        _driveScreenState.update { it.updateListItem(courseGroup) }
                    }
                }.onFailure {
                    handleError(it)
                }
            }

            DriveBottomSheetContent.CHECKPOINT_INFO -> {
                val checkPoint = _driveScreenState.value.selectedCheckPoint
                val result = withContext(Dispatchers.IO) {
                    reportCheckPointUseCase(checkPoint, reason)
                }
                result.onSuccess {
                    handler.handle(DriveEvent.REPORT_DONE)
                    _driveScreenState.update { it.clearCheckPointInfo() }
                }.onFailure {
                    handleError(it)
                }
            }

            else -> {}
        }
        _driveScreenState.update { it.replaceInfoLoading(false) }
    }

    private suspend fun refreshNearCourse(cameraState: CameraState): Result<List<Course>> {
        return withContext(Dispatchers.IO) {
            getNearByCourseUseCase(cameraState.latLng, cameraState.zoom)
        }.sucessMap { courseGroup ->
            runCatching {
                mapOverlayService.addCourseMarkerAndPath(courseGroup)
                mapOverlayService.showAllOverlays()
                courseGroup
            }
        }
    }

    private fun DriveScreenState.updateListItem(courseGroup: List<Course>): DriveScreenState {
        return copy(
            listState = listState.copy(
                listItemGroup = courseGroup.map { ListState.ListItemState(course = it) }
            )
        )
    }


    private fun CheckPointAddState.isValidateAddCheckPoint(): Result<Unit> {
        return runCatching {
            require(this.imgUriString.isNotBlank()) { AppError.ImgEmpty() }
            require(this.description.isNotEmpty()) { AppError.DescriptionEmpty() }
        }
    }

    //가이드
    private suspend fun guidePopupClick(step: DriveTutorialStep) {
        when (step) {
            DriveTutorialStep.DRIVE_GUIDE_DONE -> {
                guideMoveStepUseCase(true)
            }

            else -> {

            }
        }
    }

    private fun setTutorialStepUi(step: DriveTutorialStep) {
        _driveScreenState.update { old ->
            old.copy(
                guideState = old.guideState.copy(
                    tutorialStep = step
                )
            ).run {
                when (step) {
                    DriveTutorialStep.MOVE_TO_COURSE -> {
                        moveCamera(
                            latLng = guideCourse.cameraLatLng,
                            zoom = LIST_ITEM_ZOOM,
                            source = CameraUpdateSource.GUIDE
                        )
                    }

                    DriveTutorialStep.DRIVE_LIST_ITEM_CLICK -> {
                        setHighlightItemWithGuide(true)
                    }

                    DriveTutorialStep.MOVE_TO_LEAF -> {
                        setHighlightItemWithGuide(false)
                    }

                    DriveTutorialStep.COMMENT_FLOAT_CLICK -> {
                        copy(
                            floatingButtonState = floatingButtonState.copy(
                                highlight = DriveFloatHighlight.COMMENT
                            )
                        )
                    }

                    DriveTutorialStep.COMMENT_SHEET_DRAG -> {
                        copy(
                            floatingButtonState = floatingButtonState.copy(
                                highlight = DriveFloatHighlight.NONE
                            ),
                            popUpState = popUpState.copy(
                                commentState = popUpState.commentState.copy(
                                    isDragGuide = true
                                )
                            )
                        )
                    }

                    DriveTutorialStep.EXPORT_FLOAT_CLICK -> {
                        copy(
                            popUpState = popUpState.copy(
                                commentState = popUpState.commentState.copy(
                                    isDragGuide = false
                                )
                            ),
                            floatingButtonState = floatingButtonState.copy(
                                highlight = DriveFloatHighlight.EXPORT
                            ),
                        )
                    }

                    DriveTutorialStep.FOLD_FLOAT_CLICK -> {
                        copy(
                            guideState = guideState.copy(
                                alignment = GuideState.Companion.Align.BOTTOM_START
                            ),
                            floatingButtonState = floatingButtonState.copy(
                                highlight = DriveFloatHighlight.FOLD
                            )
                        )
                    }

                    DriveTutorialStep.SEARCHBAR_CLICK -> {
                        copy(
                            guideState = guideState.copy(
                                alignment = GuideState.Companion.Align.TOP_START
                            ),
                            searchBarState = searchBarState.copy(
                                isHighlight = true,
                                isTextGuide = true
                            ),
                            floatingButtonState = floatingButtonState.copy(
                                highlight = DriveFloatHighlight.NONE
                            ),
                        )
                    }

                    DriveTutorialStep.SEARCHBAR_EDIT -> {
                        copy(
                            searchBarState = searchBarState.copy(
                                isHighlight = false,
                                isEditBlock = true
                            )
                        )
                    }

                    DriveTutorialStep.ADDRESS_CLICK -> {
                        val highlightItemGroup = searchBarState.searchBarItemGroup.run {
                            mapIndexed { i, item ->
                                if (i == 0)
                                    item.copy(isHighlight = true)
                                else
                                    item
                            }
                        }
                        copy(
                            searchBarState = searchBarState.copy(
                                isTextGuide = false,
                                isEditBlock = false,
                                searchBarItemGroup = highlightItemGroup
                            )
                        )
                    }

                    DriveTutorialStep.DRIVE_GUIDE_DONE -> {
                        copy(
                            guideState = guideState.copy(
                                isHighlight = true
                            ),
                            searchBarState = searchBarState.copy(
                                isTextGuide = false
                            ),
                            isCongrats = true
                        )
                    }

                    DriveTutorialStep.SKIP -> {
                        copy(
                            guideState = guideState.copy(
                                isHighlight = false,
                            ),
                            isCongrats = false
                        ).moveCamera()
                    }

                    else -> {
                        this
                    }
                }
            }
        }
    }

    private fun DriveScreenState.setHighlightItemWithGuide(isHighlight: Boolean): DriveScreenState {
        val highlightItemGroup = if (isHighlight) {
            listState.listItemGroup.map { item ->
                if (item.course.courseId == guideCourse.courseId)
                    item.copy(isHighlight = true)
                else
                    item.copy(isHighlight = false)
            }
        } else {
            listState.listItemGroup.map { item ->
                item.copy(isHighlight = false)
            }
        }
        return copy(
            listState = listState.copy(
                listItemGroup = highlightItemGroup
            )
        )
    }


    //공통
    private fun lifecycleChange(event: AppLifecycle) {
        when (event) {

            AppLifecycle.onResume -> {
                loadAd()
            }

            AppLifecycle.onPause -> {
                clearAd()
            }

            else -> {}
        }
    }

    private fun eventReceive(event: AppEvent, result: Boolean) {
        when (event) {
            AppEvent.SignInScreen -> {
                if (result) {
                    clearScreen()
                    loadAd()
                }
            }

            else -> {}
        }
    }

    //유틸
    private fun loadAd() {
        val state = _driveScreenState.value
        if (state.stateMode == DriveVisibleMode.SearchBarExpand || state.floatingButtonState.stateMode == DriveFloatingVisibleMode.ExportExpand)
            viewModelScope.launch(Dispatchers.IO) {
                nativeAdServiceOld.getAd()
                    .onSuccess { newAdItemGroup ->
                        _driveScreenState.update {
                            when {
                                state.floatingButtonState.stateMode == DriveFloatingVisibleMode.ExportExpand -> {
                                    it.copy(
                                        floatingButtonState = it.floatingButtonState.copy(
                                            adItemGroup = newAdItemGroup
                                        )
                                    )
                                }

                                state.stateMode == DriveVisibleMode.SearchBarExpand -> {
                                    it.copy(searchBarState = it.searchBarState.copy(adItemGroup = newAdItemGroup))
                                }

                                else -> {
                                    it
                                }
                            }
                        }
                    }.onFailure {
                        handleError(it)
                    }
            }
    }

    private fun clearAd() {
        _driveScreenState.update {
            it.searchBarState.adItemGroup.forEach {
                it.destroy()
            }
            it.floatingButtonState.adItemGroup.forEach {
                it.destroy()
            }
            it.copy(
                searchBarState = it.searchBarState.copy(adItemGroup = emptyList()),
                floatingButtonState = it.floatingButtonState.copy(adItemGroup = emptyList())
            )
        }
    }

    private fun DriveScreenState.likeSwitch(commentId: String)
            : DriveScreenState {
        return run {
            val newCommentStateGroup =
                popUpState.commentState.commentItemGroup.map {
                    if (it.data.commentId == commentId) {
                        val oldLike = it.data.isUserLiked
                        it.copy(
                            data = it.data.copy(
                                like = it.data.like + if (oldLike) -1 else 1,
                                isUserLiked = !oldLike
                            )
                        )
                    } else
                        it
                }
            copy(
                popUpState = popUpState.copy(
                    commentState = popUpState.commentState.copy(commentItemGroup = newCommentStateGroup)
                )
            )
        }
    }

    private fun DriveScreenState.clearCourseInfo(): DriveScreenState {
        mapOverlayService.removeCourseMarkerAndPath(listOf(selectedCourse.courseId))
        mapOverlayService.removeCheckPointCluster(
            selectedCourse.courseId
        )
        return copy(
            stateMode = DriveVisibleMode.Explorer,
            floatingButtonState = floatingButtonState.copy(
                stateMode = DriveFloatingVisibleMode.Default
            ),
            selectedCourse = Course(),
        )
    }

    private fun DriveScreenState.clearCheckPointInfo(): DriveScreenState {
        mapOverlayService.removeCheckPointLeaf(
            selectedCourse.courseId,
            selectedCheckPoint.checkPointId
        )
        return copy(
            stateMode = DriveVisibleMode.CourseDetail,
            floatingButtonState = floatingButtonState.copy(
                stateMode = DriveFloatingVisibleMode.Default
            ),
            selectedCheckPoint = CheckPoint()
        )
    }

    private fun DriveScreenState.backToCourseDetail(): DriveScreenState {
        return copy(
            stateMode = DriveVisibleMode.CourseDetail,
            floatingButtonState = floatingButtonState.copy(
                stateMode = DriveFloatingVisibleMode.Default
            ),
            popUpState = PopUpState(),
            selectedCheckPoint = CheckPoint()
        )
    }

    private fun DriveScreenState.backToExplorer(): DriveScreenState {
        return copy(
            stateMode = DriveVisibleMode.Explorer,
            searchBarState = SearchBarState(),
            popUpState = PopUpState(),
            bottomSheetState = BottomSheetState(),
            floatingButtonState = FloatingButtonState(),
            selectedCourse = Course(),
            selectedCheckPoint = CheckPoint()
        )
    }

    private fun clearScreen() {
        _driveScreenState.update {
            mapOverlayService.clear()
            it.copy(
                stateMode = DriveVisibleMode.Explorer,
                searchBarState = SearchBarState(),
                popUpState = PopUpState(),
                bottomSheetState = BottomSheetState(),
                floatingButtonState = FloatingButtonState(),
                selectedCourse = Course(),
                selectedCheckPoint = CheckPoint(),
                listState = ListState()
            )
        }
    }
}