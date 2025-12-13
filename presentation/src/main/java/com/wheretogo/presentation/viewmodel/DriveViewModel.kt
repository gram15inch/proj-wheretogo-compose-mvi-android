package com.wheretogo.presentation.viewmodel

//import com.wheretogo.domain.model.dummy.guideCourse
import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheretogo.domain.LIST_ITEM_ZOOM
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.checkpoint.CheckPoint
import com.wheretogo.domain.model.comment.Comment
import com.wheretogo.domain.model.course.Course
import com.wheretogo.domain.model.util.ImageInfo
import com.wheretogo.domain.usecase.checkpoint.AddCheckpointToCourseUseCase
import com.wheretogo.domain.usecase.checkpoint.GetCheckpointForMarkerUseCase
import com.wheretogo.domain.usecase.checkpoint.RemoveCheckPointUseCase
import com.wheretogo.domain.usecase.checkpoint.ReportCheckPointUseCase
import com.wheretogo.domain.usecase.comment.AddCommentToCheckPointUseCase
import com.wheretogo.domain.usecase.comment.GetCommentForCheckPointUseCase
import com.wheretogo.domain.usecase.comment.RemoveCommentToCheckPointUseCase
import com.wheretogo.domain.usecase.comment.ReportCommentUseCase
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
import com.wheretogo.presentation.DRIVE_LIST_MIN_ZOOM
import com.wheretogo.presentation.DriveBottomSheetContent
import com.wheretogo.presentation.DriveFloatingVisibleMode
import com.wheretogo.presentation.DriveVisibleMode
import com.wheretogo.presentation.MainDispatcher
import com.wheretogo.presentation.MarkerType
import com.wheretogo.presentation.MoveAnimation
import com.wheretogo.presentation.SEARCH_MARKER
import com.wheretogo.presentation.SheetVisibleMode
import com.wheretogo.presentation.ViewModelErrorHandler
import com.wheretogo.presentation.feature.ads.AdService
import com.wheretogo.presentation.feature.geo.LocationService
import com.wheretogo.presentation.feature.map.DriveMapOverlayService
import com.wheretogo.presentation.intent.DriveScreenIntent
import com.wheretogo.presentation.model.AppMarker
import com.wheretogo.presentation.model.MarkerInfo
import com.wheretogo.presentation.model.SearchBarItem
import com.wheretogo.presentation.model.TypeEditText
import com.wheretogo.presentation.state.BottomSheetState
import com.wheretogo.presentation.state.CameraState
import com.wheretogo.presentation.state.CheckPointAddState
import com.wheretogo.presentation.state.CommentState
import com.wheretogo.presentation.state.CommentState.CommentItemState
import com.wheretogo.presentation.state.DriveScreenState
import com.wheretogo.presentation.state.DriveScreenState.Companion.popUpVisible
import com.wheretogo.presentation.state.FloatingButtonState
import com.wheretogo.presentation.state.ListState
import com.wheretogo.presentation.state.PopUpState
import com.wheretogo.presentation.state.SearchBarState
import com.wheretogo.presentation.toAppError
import com.wheretogo.presentation.toCheckPointContent
import com.wheretogo.presentation.toCommentContent
import com.wheretogo.presentation.toCommentItemState
import com.wheretogo.presentation.toItem
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
    private val errorHandler: ViewModelErrorHandler,
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
    private val mapOverlayService: DriveMapOverlayService,
    private val nativeAdService: AdService,
    private val locationService: LocationService
) : ViewModel() {
    private val _driveScreenState = MutableStateFlow(stateInit)
    val driveScreenState: StateFlow<DriveScreenState> = _driveScreenState
    private var isMapUpdate = true

    fun handleIntent(intent: DriveScreenIntent) {
        viewModelScope.launch(dispatcher) {
            when (intent) {
                //서치바
                is DriveScreenIntent.AddressItemClick -> searchBarItemClick(intent.searchBarItem)
                is DriveScreenIntent.SearchBarClick -> searchBarClick(intent.isSkipAd)
                is DriveScreenIntent.SearchBarClose -> searchBarClose()
                is DriveScreenIntent.SearchSubmit -> searchSubmit(intent.submit) // ✅

                //지도
                is DriveScreenIntent.CameraUpdated -> cameraUpdated(intent.cameraState) // ✅
                is DriveScreenIntent.MarkerClick -> markerClick(intent.appMarker) // ✅

                //목록
                is DriveScreenIntent.DriveListItemClick -> driveListItemClick(intent.itemState) // ✅

                //팝업
                is DriveScreenIntent.DismissPopupComment -> dismissCommentPopUp()
                is DriveScreenIntent.CommentListItemClick -> commentListItemClick(intent.itemState)
                is DriveScreenIntent.CommentListItemLongClick -> commentListItemLongClick(intent.comment)
                is DriveScreenIntent.CommentLikeClick -> commentLikeClick(intent.itemState) // ✅
                is DriveScreenIntent.CommentAddClick -> commentAddClick(intent.editText) // ✅
                is DriveScreenIntent.CommentRemoveClick -> commentRemoveClick(intent.comment) // ✅
                is DriveScreenIntent.CommentReportClick -> commentReportClick(intent.comment) // ✅
                is DriveScreenIntent.CommentEmogiPress -> commentEmogiPress(intent.emogi)
                is DriveScreenIntent.CommentTypePress -> commentTypePress(intent.typeEditText)

                //플로팅
                is DriveScreenIntent.CommentFloatingButtonClick -> commentFloatingButtonClick() // ✅
                is DriveScreenIntent.CheckpointAddFloatingButtonClick -> checkpointAddFloatingButtonClick()
                is DriveScreenIntent.InfoFloatingButtonClick -> infoFloatingButtonClick(intent.content)
                is DriveScreenIntent.ExportMapFloatingButtonClick -> exportMapFloatingButtonClick()
                is DriveScreenIntent.ExportMapAppButtonClick -> exportMapAppButtonClick(intent.result)
                is DriveScreenIntent.FoldFloatingButtonClick -> foldFloatingButtonClick()

                //바텀시트
                is DriveScreenIntent.BottomSheetChange -> bottomSheetChange(intent.state)
                is DriveScreenIntent.CheckpointLocationSliderChange -> checkpointLocationSliderChange(intent.percent)
                is DriveScreenIntent.CheckpointDescriptionChange -> checkpointDescriptionChange(intent.text)
                is DriveScreenIntent.CheckpointDescriptionEnterClick -> checkpointDescriptionEnterClick()
                is DriveScreenIntent.CheckpointImageChange -> checkpointImageChange(intent.imageInfo)
                is DriveScreenIntent.CheckpointSubmitClick -> checkpointSubmitClick() // ✅
                is DriveScreenIntent.InfoReportClick -> infoReportClick(intent.reason) // ✅
                is DriveScreenIntent.InfoRemoveClick -> infoRemoveClick() // ✅

                //공통
                is DriveScreenIntent.LifecycleChange -> lifecycleChange(intent.event)
                is DriveScreenIntent.EventReceive -> eventReceive(intent.event, intent.result)
                is DriveScreenIntent.BlurClick -> blurClick()

            }
        }
    }

    suspend fun handleError(error: Throwable) {
        when(errorHandler.handle(error.toAppError())){
            is AppError.InvalidState->{
                clearState()
            }
            is AppError.NeedSignIn->{
                clearAd()
                signOutUseCase()
            }
            else -> {}
        }
    }

    //서치바
    private fun searchBarItemClick(item: SearchBarItem) {
        if (item.label == CLEAR_ADDRESS || item.latlng == null) {
            mapOverlayService.removeOneTimeMarker(listOf(SEARCH_MARKER))
            _driveScreenState.update { it.initSearchBar() }
            return
        }

        mapOverlayService.removeOneTimeMarker(listOf(SEARCH_MARKER))

        // 카메라 이동
        _driveScreenState.update {
            it.run {
                copy(
                    naverMapState = naverMapState.copy(
                        cameraState = naverMapState.cameraState.copy(
                            latLng = item.latlng,
                            zoom = LIST_ITEM_ZOOM,
                            updateSource = CameraUpdateSource.SEARCH_BAR,
                            moveAnimation = MoveAnimation.APP_EASING
                        )
                    )
                )
            }
        }

        if (item.isCourse) {
            _driveScreenState.update {
                it.run {
                    copy(
                        stateMode = DriveVisibleMode.Explorer
                    ).initSearchBar()
                }
            }
            return
        } else {
            _driveScreenState.update {
                mapOverlayService.addOneTimeMarker(
                    listOf(
                        MarkerInfo(
                            contentId = SEARCH_MARKER,
                            position = item.latlng
                        )
                    )
                )
                it.copy(overlayGroup = mapOverlayService.getOverlays().toList())
            }
        }
    }

    private fun searchBarClick(isSkipAd: Boolean) {
        if(_driveScreenState.value.stateMode == DriveVisibleMode.SearchBarExpand) {
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


    }

    private fun searchBarClose() {
        mapOverlayService.removeOneTimeMarker(listOf(SEARCH_MARKER))
        _driveScreenState.update {
            it.copy(
                stateMode = DriveVisibleMode.Explorer,
                searchBarState = stateInit.searchBarState
            )
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
        }.onFailure {
            handleError(it)
        }
        _driveScreenState.update { it.replaceSearchBarLoading(false) }
    }


    //지도
    private suspend fun cameraUpdated(cameraState: CameraState) {
        val isCameraUpdate =
            _driveScreenState.value.run {
                val oldCamera = naverMapState.cameraState
                stateMode == DriveVisibleMode.Explorer &&
                        (locationService.distance(oldCamera.latLng, cameraState.latLng) >= 1 ||
                                oldCamera.zoom - cameraState.zoom >= 1 ||
                                oldCamera.updateSource != cameraState.updateSource)

            }

        val isContentsUpdate = _driveScreenState.value.run {
            isCameraUpdate && isMapUpdate
        }

        if (isCameraUpdate)
            _driveScreenState.update {
                it.copy(
                    naverMapState = it.naverMapState.copy(
                        cameraState = cameraState.copy(
                            updateSource = CameraUpdateSource.USER,
                            zoom = cameraState.zoom
                        )
                    )
                )
            }

        val course = _driveScreenState.value.selectedCourse
        if(course.checkpointIdGroup.isNotEmpty()){
            if (_driveScreenState.value.stateMode == DriveVisibleMode.CourseDetail) {
                mapOverlayService.scaleToPointLeafInCluster(
                    course.courseId,
                    cameraState.latLng,
                )
            }
        }


        if (isContentsUpdate) {
            isMapUpdate = false
            _driveScreenState.update { it.replaceScreenLoading(true) }
            _driveScreenState.update { it.refreshNearCourse(cameraState) }
            isMapUpdate = true
            _driveScreenState.update { it.replaceScreenLoading(false) }
        }

        if (_driveScreenState.value.stateMode == DriveVisibleMode.CourseDetail &&
            cameraState.zoom <= COURSE_DETAIL_MIN_ZOOM
        ) {
            foldFloatingButtonClick()
        }
    }

    private fun markerClick(appMarker: AppMarker){
        when(appMarker.markerInfo.type){
            MarkerType.SPOT -> courseMarkerClick(appMarker)
            MarkerType.CHECKPOINT ->{ /* 체크포인트 클릭은 클러스터 생성시 전달 아래는 테스트용 */
                checkPointLeafClick(appMarker.markerInfo.contentId)
            }
            else-> {}
        }

    }

    private fun courseMarkerClick(appMarker: AppMarker) {
        _driveScreenState.update {
            it.run {
                val zoom =
                    if (naverMapState.cameraState.zoom > DRIVE_LIST_MIN_ZOOM) naverMapState.cameraState.zoom else DRIVE_LIST_MIN_ZOOM + 0.1
                val latlng = appMarker.markerInfo.position
                if(latlng==null)
                    return
                val newCameraState =
                    CameraState(
                        latLng = latlng,
                        zoom = zoom,
                        updateSource = CameraUpdateSource.MARKER,
                        moveAnimation = MoveAnimation.APP_EASING
                    )
                copy(
                    naverMapState = naverMapState.copy(cameraState = newCameraState)
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
                if (DriveScreenState.bottomSheetVisible.contains(stateMode))
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
        latLng: LatLng,
        zoom: Double? = null,
        source: CameraUpdateSource,
        animation: MoveAnimation = MoveAnimation.APP_LINEAR
    ): DriveScreenState {
        return if (latLng != LatLng()) {
            run {
                val oldZoom = zoom ?: naverMapState.cameraState.zoom
                val newZoom = when {
                    oldZoom <= LIST_ITEM_ZOOM -> LIST_ITEM_ZOOM
                    else -> oldZoom
                }

                copy(
                    naverMapState = naverMapState.copy(
                        cameraState = naverMapState.cameraState.copy(
                            latLng = latLng,
                            zoom = newZoom,
                            updateSource = source,
                            moveAnimation = animation
                        )
                    )
                )
            }
        } else
            copy()
    }

    //목록
    private suspend fun driveListItemClick(listItemState: ListState.ListItemState) {
        val course = listItemState.course

        // 코스 포커스 & 카메라 이동
        _driveScreenState.update {
            mapOverlayService.focusAndHideOthers(course)
            it.run {
                copy(
                    stateMode = DriveVisibleMode.CourseDetail,
                    isLoading = true,
                    overlayGroup = mapOverlayService.getOverlays().toList(),
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
                        _driveScreenState.value.naverMapState.cameraState.latLng
                    )
                },
                onLeafClick = ::checkPointLeafClick
            )
            it.copy(
                isLoading = false,
                overlayGroup = mapOverlayService.getOverlays().toList()
            )
        }
    }

    //팝업
    private fun blurClick() {
        _driveScreenState.update {
            if (it.selectedCheckPoint.checkPointId.isNotBlank()
                && (it.stateMode == DriveVisibleMode.BlurBottomSheetExpand)
            )
                it.backToCheckPointDetail()
            else
                it.backToCourseDetail()
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

    private fun commentListItemClick(itemState: CommentItemState) {
        val comment = itemState.data
        _driveScreenState.switchFold(comment.commentId)
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

    private suspend fun commentLikeClick(itemState: CommentItemState) {
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
            }.onSuccess {
                val caption =
                    it.firstOrNull { it.checkPointId == newComment.groupId }?.caption ?: ""
                mapOverlayService.updateCheckPointLeafCaption(course.courseId,content.groupId, caption)
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
                mapOverlayService.updateCheckPointLeafCaption(course.courseId ,comment.groupId, caption)
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
                mapOverlayService.updateCheckPointLeafCaption(course.courseId,comment.groupId, caption)
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

    private fun MutableStateFlow<DriveScreenState>.switchFold(commentId: String) {
        update {
            val oldCommentGroup =
                it.popUpState.commentState.commentItemGroup
            val newCommentGroup =
                oldCommentGroup.map {
                    if (it.data.commentId == commentId && it.data.detailedReview.isNotBlank()) {
                        it.copy(isFold = !it.isFold)
                    } else {
                        it
                    }
                }
            it.copy(
                popUpState = it.popUpState.copy(
                    commentState = it.popUpState.commentState.copy(
                        commentItemGroup = newCommentGroup,
                        commentSettingState = CommentState.CommentSettingState()
                    )
                )
            )
        }
    }

    private fun commentEmogiPress(emogi: String) {
        _driveScreenState.update {
            it.run {
                copy(
                    popUpState = popUpState.copy(
                        commentState = popUpState.commentState.copy(
                            commentAddState = popUpState.commentState.commentAddState.copy(
                                largeEmoji = emogi
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
                                oneLinePreview = "",
                                detailReview = typeEditText.editText,
                                isLargeEmogi = true,
                                isEmogiGroup = true,
                            )
                        }
                    }

                    CommentType.DETAIL -> {
                        popUpState.commentState.commentAddState.run {
                            copy(
                                commentType = typeEditText.type,
                                oneLineReview = typeEditText.editText,
                                detailReview = "",
                                oneLinePreview =
                                    "${largeEmoji.ifEmpty { emogiGroup.firstOrNull() ?: "" }}  ${typeEditText.editText}",
                                isLargeEmogi = false,
                                isEmogiGroup = false,
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


    //플로팅
    private suspend fun commentFloatingButtonClick() {
        val state = _driveScreenState.value
        val checkpoint = _driveScreenState.value.selectedCheckPoint
        if (state.floatingButtonState.stateMode == DriveFloatingVisibleMode.Hide) {
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
        }
        // 코멘트 관련 Ui 표시 및 로딩 시작
        _driveScreenState.update {
            it.run {
                copy(
                    popUpState = popUpState.copy(
                        commentState = popUpState.commentState.copy(
                            isVisible = true
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
                    if (popUpState.commentState.isVisible)
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
                overlayGroup = mapOverlayService.getOverlays().toList(),
                stateMode = DriveVisibleMode.BottomSheetExpand,
                bottomSheetState = it.bottomSheetState.copy(
                    content = DriveBottomSheetContent.CHECKPOINT_ADD,
                )
            ).initCheckPointAddState()
        }
    }

    private fun infoFloatingButtonClick(content: DriveBottomSheetContent) {
        _driveScreenState.update {
            it.run {
                copy(
                    stateMode = DriveVisibleMode.BlurBottomSheetExpand,
                    bottomSheetState = bottomSheetState.copy(
                        content = content,
                    ),
                    popUpState = popUpState.copy(
                        commentState = popUpState.commentState.copy(
                            isVisible = false
                        )
                    ),
                    floatingButtonState = floatingButtonState.copy(
                        stateMode = DriveFloatingVisibleMode.Hide
                    ),
                ).initInfoState()
            }
        }
    }

    private fun exportMapFloatingButtonClick() {
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
        }
    }

    private suspend fun exportMapAppButtonClick(result: Result<Unit>) {
        result.onFailure {
            handleError(it)
        }
    }

    private fun foldFloatingButtonClick() {
        val course = _driveScreenState.value.selectedCourse
        _driveScreenState.update {
            mapOverlayService.removeCheckPointCluster(course.courseId)
            mapOverlayService.showAllOverlays()
            it.copy(
                overlayGroup = mapOverlayService.getOverlays().toList()
            ).backToExplorer()
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun bottomSheetChange(state: SheetVisibleMode) {
        val content = _driveScreenState.value.bottomSheetState.content
        val course = _driveScreenState.value.selectedCourse
        when (state) {
            SheetVisibleMode.Expand -> {}

            SheetVisibleMode.Expanded -> {
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

            SheetVisibleMode.PartiallyExpand -> {
                if (_driveScreenState.value.bottomSheetState.isUserControl)
                    when (content) {
                        DriveBottomSheetContent.CHECKPOINT_ADD -> {
                            _driveScreenState.update {
                                mapOverlayService.removeOneTimeMarker(listOf(CHECKPOINT_ADD_MARKER))
                                it.copy(
                                    stateMode = DriveVisibleMode.CourseDetail,
                                    floatingButtonState = it.floatingButtonState.copy(
                                        stateMode = DriveFloatingVisibleMode.Default
                                    )
                                )
                            }
                        }

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

            SheetVisibleMode.PartiallyExpanded -> {
                when (content) {
                    DriveBottomSheetContent.CHECKPOINT_ADD -> {
                        _driveScreenState.update {
                            it.moveCamera(
                                course.cameraLatLng,
                                source = CameraUpdateSource.BOTTOM_SHEET_DOWN
                            ).copy(
                                bottomSheetState = BottomSheetState()
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
            }
        }
    }

    //바텀시트

    private fun checkpointLocationSliderChange(percent: Float) {
        _driveScreenState.update {
            val points = it.selectedCourse.points
            val index = round((points.size - 1) * percent).toInt()
            val newLatlng = points[index]
            mapOverlayService.updateOneTimeMarker(
                MarkerInfo(
                    contentId = CHECKPOINT_ADD_MARKER,
                    type = MarkerType.DEFAULT,
                    position = newLatlng
                )
            )
            val newCheckPointAddState = it.bottomSheetState.checkPointAddState.run {
                copy(
                    latLng = newLatlng,
                    sliderPercent = percent,
                    isSubmitActive = isValidateAddCheckPoint().isSuccess
                )
            }

            it.copy(bottomSheetState = it.bottomSheetState.copy(checkPointAddState = newCheckPointAddState))
        }
    }

    private fun checkpointDescriptionChange(text: String) {
        _driveScreenState.update {
            val newCheckPointAddState = it.bottomSheetState.checkPointAddState.run {
                copy(
                    description = text,
                    isSubmitActive = isValidateAddCheckPoint().isSuccess
                )
            }
            it.copy(bottomSheetState = it.bottomSheetState.copy(checkPointAddState = newCheckPointAddState))
        }
    }

    private fun checkpointImageChange(imageInfo: ImageInfo) {
        _driveScreenState.update {
            val newCheckPointAddState = it.bottomSheetState.checkPointAddState.run {
                copy(
                    imgUriString = imageInfo.uriString,
                    imgInfo = imageInfo,
                    isSubmitActive = isValidateAddCheckPoint().isSuccess
                )
            }
            it.copy(bottomSheetState = it.bottomSheetState.copy(checkPointAddState = newCheckPointAddState))
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
                        overlayGroup = mapOverlayService.getOverlays().toList(),
                        selectedCourse = course.copy(
                            checkpointIdGroup = newCheckpointIdGroup
                        ),
                        floatingButtonState = floatingButtonState.copy(
                            stateMode = DriveFloatingVisibleMode.Default
                        ),
                        bottomSheetState = bottomSheetState.copy(
                            isUserControl = false
                        )
                    )
                }
            }
        }.onFailure {
            handleError(it)
            _driveScreenState.update { it.replaceCheckpointAddLoading(false) }
        }
    }

    private fun checkpointDescriptionEnterClick() {}

    private suspend fun infoRemoveClick() {
        val content = _driveScreenState.value.bottomSheetState.content

        _driveScreenState.update { it.replaceInfoLoading(true) }
        when (content) {
            DriveBottomSheetContent.COURSE_INFO -> {
                val course = _driveScreenState.value.selectedCourse
                withContext(Dispatchers.IO) { removeCourseUseCase(course.courseId) }.onSuccess {
                    _driveScreenState.update {
                        it.clearCourseInfo()
                            .refreshNearCourse(it.naverMapState.cameraState)
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
        _driveScreenState.update { it.replaceInfoLoading(true) }

        when (content) {
            DriveBottomSheetContent.COURSE_INFO -> {
                val course = _driveScreenState.value.selectedCourse
                val result = withContext(Dispatchers.IO) {
                    reportCourseUseCase(course, reason)
                }
                result.onSuccess {
                    _driveScreenState.update {
                        it.clearCourseInfo()
                            .refreshNearCourse(it.naverMapState.cameraState)
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
                    _driveScreenState.update { it.clearCheckPointInfo() }
                }.onFailure {
                    handleError(it)
                }
            }

            else -> {}
        }
        _driveScreenState.update { it.replaceInfoLoading(false) }
    }

    private suspend fun DriveScreenState.refreshNearCourse(cameraState: CameraState): DriveScreenState {
        val newCourseGroup = withContext(Dispatchers.IO) {
            getNearByCourseUseCase(cameraState.latLng, cameraState.zoom)
        }

        val listItemGroup = if (cameraState.zoom >= DRIVE_LIST_MIN_ZOOM) {
            newCourseGroup.filterNearByListGroup(center = cameraState.latLng, meter = 3000)
        } else emptyList()

        return run {
            mapOverlayService.addCourseMarkerAndPath(newCourseGroup)
            mapOverlayService.showAllOverlays()
            copy(
                overlayGroup = mapOverlayService.getOverlays().toList(),
                listState = listState.copy(
                    listItemGroup = listItemGroup
                )
            )
        }
    }

    private fun CheckPointAddState.isValidateAddCheckPoint(): Result<Unit> {
        return runCatching {
            require(this.imgUriString.isNotBlank()) { AppError.ImgEmpty() }
            require(this.description.isNotEmpty()) { AppError.DescriptionEmpty() }
        }
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
                if(result){
                    clearState()
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
                nativeAdService.getAd()
                    .onSuccess { newAdItemGroup ->
                        _driveScreenState.update {
                            when {
                                state.floatingButtonState.stateMode == DriveFloatingVisibleMode.ExportExpand -> {
                                    it.copy(
                                        floatingButtonState = it.floatingButtonState.copy(
                                            adItemGroup = newAdItemGroup.toItem()
                                        )
                                    )
                                }

                                state.stateMode == DriveVisibleMode.SearchBarExpand -> {
                                    it.copy(searchBarState = it.searchBarState.copy(adItemGroup = newAdItemGroup.toItem()))
                                }

                                else -> { it }
                            }
                        }
                    }.onFailure {
                        handleError(it)
                    }
            }
    }

    private fun clearAd(){
        _driveScreenState.value.destroyAd()
        _driveScreenState.update {
            it.copy(
                searchBarState = it.searchBarState.copy(adItemGroup = emptyList()),
                floatingButtonState = it.floatingButtonState.copy(adItemGroup = emptyList())
            )
        }
    }

    private fun DriveScreenState.destroyAd() {
        searchBarState.adItemGroup.forEach {
            it.nativeAd.destroy()
        }
        floatingButtonState.adItemGroup.forEach {
            it.nativeAd.destroy()
        }
    }

    private fun List<Course>.filterNearByListGroup(
        center: LatLng,
        meter: Int
    ): List<ListState.ListItemState> {
        return mapNotNull {
            val course = it.cameraLatLng
            val distance = locationService.distance(center, course)
            if (distance < meter) // 근처 코스만 필터링
                ListState.ListItemState(
                    distanceFromCenter = distance,
                    course = it
                )
            else
                null
        }.sortedBy { it.course.courseId }
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
            bottomSheetState = bottomSheetState.copy(
                isUserControl = false
            ),
            selectedCourse = Course(),
        )
    }

    private fun DriveScreenState.clearCheckPointInfo(): DriveScreenState {
        mapOverlayService.removeCheckPointLeaf(selectedCourse.courseId,selectedCheckPoint.checkPointId)
        return copy(
            stateMode = DriveVisibleMode.CourseDetail,
            overlayGroup = overlayGroup.filter { it.contentId != selectedCheckPoint.checkPointId },
            floatingButtonState = floatingButtonState.copy(
                stateMode = DriveFloatingVisibleMode.Default
            ),
            bottomSheetState = bottomSheetState.copy(
                isUserControl = false
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

    private fun DriveScreenState.backToCheckPointDetail(): DriveScreenState {
        return copy(
            stateMode = DriveVisibleMode.BlurCheckpointDetail,
            floatingButtonState = floatingButtonState.copy(
                stateMode = DriveFloatingVisibleMode.Popup
            )
        )
    }

    private fun DriveScreenState.backToExplorer(): DriveScreenState{
        return copy(
            searchBarState = SearchBarState(),
            popUpState = PopUpState(),
            bottomSheetState = BottomSheetState(),
            floatingButtonState = FloatingButtonState(),
            stateMode = DriveVisibleMode.Explorer,
            selectedCourse = Course(),
            selectedCheckPoint = CheckPoint()
        )
    }

    private fun clearState(){
        _driveScreenState.update {
            mapOverlayService.clear()
            it.backToExplorer().copy(
                overlayGroup = emptyList(),
                listState = ListState()
            )
        }
    }
}
