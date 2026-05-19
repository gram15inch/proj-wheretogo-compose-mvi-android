package com.wheretogo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheretogo.domain.DriveTutorialStep
import com.wheretogo.domain.MarkerType
import com.wheretogo.domain.ZOOM
import com.wheretogo.domain.handler.DriveHandler
import com.wheretogo.domain.handler.DriveMsgEvent
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.checkpoint.CheckPoint
import com.wheretogo.domain.model.comment.Comment
import com.wheretogo.domain.model.course.Course
import com.wheretogo.domain.model.dummy.guideCourse
import com.wheretogo.domain.model.map.CameraMoveTrigger
import com.wheretogo.domain.model.map.MarkerInfo
import com.wheretogo.domain.model.map.MoveAnimation
import com.wheretogo.domain.model.map.MoveCameraOption
import com.wheretogo.domain.model.map.SlideItem
import com.wheretogo.domain.model.report.ReportReason
import com.wheretogo.domain.model.report.ReportType
import com.wheretogo.domain.model.util.AppCache
import com.wheretogo.domain.model.util.ImageInfo
import com.wheretogo.domain.repository.DefaultMapId
import com.wheretogo.domain.repository.MapContentRepository
import com.wheretogo.domain.usecase.app.DriveTutorialUseCase
import com.wheretogo.domain.usecase.app.ObserveSettingsUseCase
import com.wheretogo.domain.usecase.checkpoint.AddCheckpointToCourseUseCase
import com.wheretogo.domain.usecase.checkpoint.RemoveCheckPointUseCase
import com.wheretogo.domain.usecase.comment.AddCommentToCheckPointUseCase
import com.wheretogo.domain.usecase.comment.GetCommentForCheckPointUseCase
import com.wheretogo.domain.usecase.comment.RemoveCommentToCheckPointUseCase
import com.wheretogo.domain.usecase.course.RemoveCourseUseCase
import com.wheretogo.domain.usecase.report.ReportContentUseCase
import com.wheretogo.domain.usecase.user.UserSignOutUseCase
import com.wheretogo.domain.usecase.util.ClearCacheUseCase
import com.wheretogo.domain.usecase.util.GetImageForPopupUseCase
import com.wheretogo.domain.usecase.util.SearchKeywordUseCase
import com.wheretogo.domain.usecase.util.UpdateLikeUseCase
import com.wheretogo.presentation.AppError
import com.wheretogo.presentation.AppEvent
import com.wheretogo.presentation.AppLifecycle
import com.wheretogo.presentation.CHECKPOINT_ADD_MARKER
import com.wheretogo.presentation.CLEAR_ADDRESS
import com.wheretogo.presentation.CommentType
import com.wheretogo.presentation.DriveBottomSheetContent
import com.wheretogo.presentation.DriveFloatingVisibleMode
import com.wheretogo.presentation.DriveVisibleMode
import com.wheretogo.presentation.MainDispatcher
import com.wheretogo.presentation.SEARCH_MARKER
import com.wheretogo.presentation.SheetVisibleMode
import com.wheretogo.presentation.event.DriveEvent
import com.wheretogo.presentation.event.DriveEvent.Companion.addLeaf
import com.wheretogo.presentation.event.DriveEvent.Companion.addMarker
import com.wheretogo.presentation.event.DriveEvent.Companion.deleteMarker
import com.wheretogo.presentation.event.DriveEvent.Companion.deleteSelectCheckPoint
import com.wheretogo.presentation.event.DriveEvent.Companion.deleteSelectCourse
import com.wheretogo.presentation.event.DriveEvent.Companion.refreshCluster
import com.wheretogo.presentation.event.DriveEvent.Companion.refreshCourses
import com.wheretogo.presentation.event.DriveEvent.Companion.stopCameraWhenSheetChange
import com.wheretogo.presentation.event.DriveEvent.Companion.updateMarker
import com.wheretogo.presentation.feature.ads.AdService
import com.wheretogo.presentation.feature.executeAction
import com.wheretogo.presentation.feature.executeActionWithUpdateUi
import com.wheretogo.presentation.feature.guide.toStepState
import com.wheretogo.presentation.intent.DriveScreenIntent
import com.wheretogo.domain.model.course.CourseDirectionItem
import com.wheretogo.presentation.model.SearchBarItem
import com.wheretogo.presentation.model.TypeEditText
import com.wheretogo.presentation.state.BottomSheetState
import com.wheretogo.presentation.state.CheckPointAddState
import com.wheretogo.presentation.state.CommentState
import com.wheretogo.presentation.state.DriveScreenState
import com.wheretogo.presentation.state.FloatingButtonState
import com.wheretogo.presentation.state.ListState
import com.wheretogo.presentation.state.PopUpState
import com.wheretogo.presentation.state.SearchBarState
import com.wheretogo.presentation.toAppError
import com.wheretogo.presentation.toCommentContent
import com.wheretogo.presentation.toContent
import com.wheretogo.presentation.toItemState
import com.wheretogo.presentation.toNavigation
import com.wheretogo.presentation.toSearchBarItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
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
    private val getCommentForCheckPointUseCase: GetCommentForCheckPointUseCase,
    private val getImageForPopupUseCase: GetImageForPopupUseCase,
    private val addCheckpointToCourseUseCase: AddCheckpointToCourseUseCase,
    private val addCommentToCheckPointUseCase: AddCommentToCheckPointUseCase,
    private val removeCourseUseCase: RemoveCourseUseCase,
    private val removeCheckPointUseCase: RemoveCheckPointUseCase,
    private val removeCommentToCheckPointUseCase: RemoveCommentToCheckPointUseCase,
    private val reportContentUseCase: ReportContentUseCase,
    private val updateLikeUseCase: UpdateLikeUseCase,
    private val searchKeywordUseCase: SearchKeywordUseCase,
    private val driveTutorialUseCase: DriveTutorialUseCase,
    private val signOutUseCase: UserSignOutUseCase,
    private val clearCacheUseCase: ClearCacheUseCase,
    private val nativeAdService: AdService,
    private val mapContentRepository: MapContentRepository
) : ViewModel() {
    private val _driveScreenState = MutableStateFlow(stateInit)
    val driveScreenState: StateFlow<DriveScreenState> = _driveScreenState
    private val _driveEvent = MutableSharedFlow<DriveEvent>()
    val driveEvent: SharedFlow<DriveEvent> = _driveEvent

    fun handleIntent(intent: DriveScreenIntent) {
        viewModelScope.launch(dispatcher) {
            when (intent) {
                //서치바
                is DriveScreenIntent.AddressItemClick -> searchBarItemClick(intent.searchBarItem)
                is DriveScreenIntent.SearchBarClick -> searchBarClick(intent.isSkipAd)
                is DriveScreenIntent.SearchBarClose -> searchBarClose()
                is DriveScreenIntent.SearchSubmit -> searchSubmit(intent.submit)

                //목록
                is DriveScreenIntent.DriveListItemClick -> driveListItemClick(intent.item)

                //팝업
                is DriveScreenIntent.DismissPopupComment -> dismissCommentPopUp()
                is DriveScreenIntent.PopupImageSlide -> popupImageSlide(intent.index)
                is DriveScreenIntent.CommentListItemClick -> commentListItemClick(intent.itemState)
                is DriveScreenIntent.CommentListItemLongClick -> commentListItemLongClick(intent.comment)
                is DriveScreenIntent.CommentAddClick -> commentAddClick(intent.editText)
                is DriveScreenIntent.CommentLikeClick -> commentLikeClick(intent.itemState)
                is DriveScreenIntent.CommentRemoveClick -> commentRemoveClick(intent.comment)
                is DriveScreenIntent.CommentReportClick -> commentReportClick(intent.comment, intent.reason)
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

                //가이드
                is DriveScreenIntent.GuidePopupClick -> guidePopupClick(intent.step)

                //디버그
                is DriveScreenIntent.DebugOverlayClick -> debugOverlayClick()
            }
        }
    }

    init {
        observe()
    }

    override fun onCleared() {
        mapContentRepository.clear()
    }

    fun observe() {
        viewModelScope.launch(dispatcher) {
            if(stateInit.isObserveSetting) // 테스트시 업데이트 생략 용도
                launch {
                    observeSettingsUseCase()
                        .collect { settings ->
                            settings.onSuccess { set ->
                                setTutorialStepUi(set.tutorialStep)
                            }
                        }
                }
            launch {
                mapContentRepository.courseList.drop(1)
                    .collect { courses ->
                        _driveScreenState.update {
                            it.updateListItem(courses)
                        }

                        driveTutorialUseCase(DriveTutorialStep.MOVE_TO_COURSE, courses)
                    }
            }
            launch {
                mapContentRepository.selectedCourseState.drop(1).collect{ item->
                    _driveScreenState.update {
                        if (item == null) {
                            it.backToExplorer()
                        } else {
                            it.copy(
                                floatingButtonState = it.floatingButtonState.copy(
                                    navigation = item.course.toNavigation()
                                )
                            )
                        }
                    }

                    driveTutorialUseCase(DriveTutorialStep.FOLD_FLOAT_CLICK)
                }
            }
            launch {
                mapContentRepository.selectedCheckPointState.drop(1).collect { cp ->
                    val cps = mapContentRepository.checkPointList.value
                    val initPage = cps.indexOf(cp).takeIf { it != -1 } ?: 0
                    val items = createSlideItems(cps)
                    _driveScreenState.update {
                        if (cp == null) {
                            it.backToCourseDetail()
                        } else {
                            it.copy(
                                popUpState = it.popUpState.copy(
                                    initPage = initPage,
                                    slideItems = items
                                ),
                                floatingButtonState = it.floatingButtonState.copy(
                                    stateMode = DriveFloatingVisibleMode.Popup
                                ),
                                stateMode = DriveVisibleMode.BlurCheckpointDetail
                            )
                        }
                    }
                }
            }
        }
    }

    private suspend fun createSlideItems(cps:List<CheckPoint>): List<SlideItem> {
        val items = withContext(Dispatchers.IO) {
            cps.mapIndexed { idx, cp ->
                async {
                    SlideItem(
                        contentId = cp.checkPointId,
                        title = cp.description,
                        subtitle = cp.userName,
                        imageId = cp.imageId
                    )
                }
            }
        }.awaitAll()

        return items
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

            is AppError.UnexpectedException -> {
                handler.handle(DriveMsgEvent.UNKNOWN_ERR)
            }

            else -> {}
        }
    }

    //서치바
    private suspend fun searchBarItemClick(item: SearchBarItem) {
        if (item.label == CLEAR_ADDRESS || item.latlng == null) {
            searchBarClose()
            return
        }

        // 카메라 이동
        _driveEvent.emit(DriveEvent.MoveCamera(item.toMoveCameraOption()))

        if (item.isCourse) {
            searchBarClose()
        } else {
            _driveEvent.addMarker(item.toMarkerInfo())
        }

        driveTutorialUseCase(DriveTutorialStep.ADDRESS_CLICK)
    }

    private suspend fun searchBarClick(isSkipAd: Boolean) {
        _driveScreenState.update {
            it.run {
                copy(
                    stateMode = DriveVisibleMode.SearchBarExpand,
                    searchBarState = searchBarState.copy(
                        isActive = true,
                        isAdVisible = true,
                        searchBarItemGroup = emptyList()
                    )
                )
            }

        }


        if (!isSkipAd && _driveScreenState.value.searchBarState.adItemGroup.isEmpty()) {
            loadAdOnSearchBarExpand()
        }

        driveTutorialUseCase(DriveTutorialStep.SEARCHBAR_CLICK)
    }

    private suspend fun searchSubmit(address: String) {
        if (address.trim().isBlank()) { // 주소 없을시 검색창 비활성화
            searchBarClose()
            return
        }
        _driveScreenState.executeAction(
            loading = { state, isLoading -> state.replaceSearchBarLoading(isLoading) },
            action = { searchKeywordUseCase(address) },
            onSuccess = { addressGroup ->
                _driveScreenState.update {
                    it.copy(
                        searchBarState = it.searchBarState.copy(
                            isEmptyVisible = addressGroup.isEmpty(),
                            searchBarItemGroup = addressGroup.map { it.toSearchBarItem() }
                        )
                    )
                }

                driveTutorialUseCase(DriveTutorialStep.SEARCHBAR_EDIT)
            },
            onFailure = { handleError(it) }
        )
    }

    private suspend fun searchBarClose() {
        _driveEvent.deleteMarker(SEARCH_MARKER)
        _driveScreenState.update {
            it.copy(stateMode = DriveVisibleMode.Explorer)
                .initSearchBar()
        }
    }



    //목록
    private suspend fun driveListItemClick(item: CourseDirectionItem) {
        driveTutorialUseCase(DriveTutorialStep.DRIVE_LIST_ITEM_CLICK)

        // 코스 포커스
        _driveEvent.emit(DriveEvent.Focus(item))

        _driveScreenState.update {
            it.run {
                copy(
                    floatingButtonState = floatingButtonState.copy(
                        stateMode = DriveFloatingVisibleMode.Default
                    ),
                    stateMode = DriveVisibleMode.CourseDetail,
                    isLoading = true
                )
            }
        }
    }

    //팝업
    private fun dismissCommentPopUp() {
        _driveScreenState.update {
            it.copy(
                popUpState = it.popUpState.copy(
                    commentState = CommentState()
                ),
                floatingButtonState = it.floatingButtonState.copy(
                    stateMode = DriveFloatingVisibleMode.Popup
                )
            )
        }
    }

    private suspend fun popupImageSlide(index: Int) {
       runCatching {
           val items = _driveScreenState.value.popUpState.slideItems
           val checkPointId = items[index].contentId?:throw Exception("empty $index")

           coroutineScope {
               // 이미지 가져오기
               val img = async {
                   items.refreshImageUrl(index)
               }

               // 댓글 가져오기
               val comment = async {
                   if (_driveScreenState.value.popUpState.commentState.isContentVisible)
                       refreshCommentList(checkPointId, withUiUpdate = false)
                   else null
               }

               val items = img.await()
               val comments = comment.await()
               _driveScreenState.update {
                   it.copy(
                       popUpState = it.popUpState.copy(
                           slideItems = items,
                           commentState = it.popUpState.commentState.copy(
                               commentItemGroup = comments
                                   ?.map { cmt -> cmt.toItemState() }
                           )
                       )
                   )
               }
           }
       }
    }

    private suspend fun List<SlideItem>.refreshImageUrl(index: Int): List<SlideItem> {
        return withContext(Dispatchers.IO) {
            val isPrePatchIdx = listOf(index - 1, index, index + 1)
            mapIndexed { idx, item ->
                async {
                    if (!isPrePatchIdx.contains(idx)) return@async item
                    val imageId = item.imageId
                    if (imageId.isNullOrBlank() || !item.url.isNullOrBlank()) return@async item
                    val url = getImageForPopupUseCase(imageId)
                    item.copy(url = url)
                }
            }
        }.awaitAll()
    }

    private fun commentListItemClick(itemState: CommentState.CommentItemState) {
        val comment = itemState.data
        _driveScreenState.update {
            it.switchFold(comment.commentId)
        }
    }

    private fun commentListItemLongClick(comment: Comment) {
        val setting =
            if (_driveScreenState.value.popUpState.commentState.commentSettingState == null)
                CommentState.CommentSettingState(comment = comment) else null
        _driveScreenState.update {
            it.run {
                copy(
                    popUpState = popUpState.copy(
                        commentState = popUpState.commentState.copy(
                            commentSettingState = setting
                        ),
                    )
                )
            }

        }
    }

    private suspend fun commentAddClick(editText: String) {
        val content = _driveScreenState.value.popUpState.commentState.commentAddState
            .toCommentContent(editText)
        if (content.oneLineReview.isBlank()) return

        _driveScreenState.update { it.replaceCommentAddStateLoading(true) }
        withContext(Dispatchers.IO) {
            addCommentToCheckPointUseCase(content)
        }.onSuccess { newComment ->
            _driveScreenState.updateComment(newComment, true)
        }.onFailure {
            handleError(it)
        }
        _driveScreenState.update { it.initCommentAddState() }
    }

    private suspend fun commentLikeClick(itemState: CommentState.CommentItemState) {
        val commentId = itemState.data.commentId
        _driveScreenState.executeActionWithUpdateUi(
            loading = { state, isLoading-> state.replaceCommentLoading(commentId, isLoading) },
            onBeforeActionUi = { it.likeSwitch(commentId) },
            action = { updateLikeUseCase(comment = itemState.data, isLike = !itemState.data.isUserLiked) },
            onFailure = { handleError(it) },
            onFailureUi = { it.likeSwitch(commentId) },
        )
    }

    private suspend fun commentRemoveClick(comment: Comment) {
        _driveScreenState.executeActionWithUpdateUi(
            loading = { state, isLoading -> state.replaceCommentSettingLoading(isLoading) },
            action = { removeCommentToCheckPointUseCase(comment.groupId, comment.commentId) },
            onSuccessUi = { it.updateComment(comment, false).closeCommentSetting() },
            onSuccess = { _driveEvent.refreshCluster(DefaultMapId.SELECT_COURSE_ID.name) },
            onFailure = { handleError(it) }
        )
    }

    private suspend fun commentReportClick(comment: Comment, reason: ReportReason) {
        _driveScreenState.executeActionWithUpdateUi(
            loading = { state, isLoading -> state.replaceCommentSettingLoading(isLoading) },
            action = { reportContentUseCase(comment.toReportContent(reason)) },
            onSuccessUi = { it.updateComment(comment, false).closeCommentSetting() },
            onSuccess = { _driveEvent.refreshCluster(DefaultMapId.SELECT_COURSE_ID.name) },
            onFailure = { handleError(it) }
        )
    }

    private fun MutableStateFlow<DriveScreenState>.updateComment(comment: Comment, isAdd: Boolean) {
        val oldCommentGroup = this.value.popUpState.commentState.commentItemGroup ?: return
        val newCommentGroup = if (isAdd) {
            oldCommentGroup + comment.toItemState()
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

    private fun DriveScreenState.updateComment(comment: Comment, isAdd: Boolean): DriveScreenState {
        val oldCommentGroup = popUpState.commentState.commentItemGroup ?: return this
        val newCommentGroup = if (isAdd) {
            oldCommentGroup + comment.toItemState()
        } else {
            oldCommentGroup.filter { it.data.commentId != comment.commentId }
        }
        return run {
            copy(
                popUpState = popUpState.copy(
                    commentState = popUpState.commentState.copy(
                        commentItemGroup = newCommentGroup
                    )
                )
            )
        }
    }

    private fun DriveScreenState.switchFold(commentId: String): DriveScreenState {
        val oldCommentGroup =
            popUpState.commentState.commentItemGroup
        val newCommentGroup =
            oldCommentGroup?.map {
                if (it.data.commentId == commentId && it.data.detailedReview.isNotBlank()) {
                    it.copy(isFold = !it.isFold)
                } else {
                    it
                }
            }
        return copy(
            popUpState = popUpState.copy(
                commentState = popUpState.commentState.copy(
                    commentItemGroup = newCommentGroup
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
        val checkpoint = mapContentRepository.selectedCheckPointState.value

        if (checkpoint?.checkPointId.isNullOrEmpty())
            return clearScreen()

        // 가이드
        driveTutorialUseCase(DriveTutorialStep.COMMENT_FLOAT_CLICK)

        // 코멘트 관련 Ui 표시
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
                )
            }
        }


        // 코멘트 가져오기
        refreshCommentList(
            checkPointId = checkpoint.checkPointId,
            delay = 200 // 바텀시트 에니메이션 대기
        )
    }

    private suspend fun checkpointAddFloatingButtonClick() {
        runCatching {
            val courseItem = mapContentRepository.selectedCourseState.value
            val initLatlng = courseItem?.course?.waypoints?.firstOrNull()?: throw Exception("missing initLatlng")
            _driveEvent.addMarker(
                markerInfo = MarkerInfo(
                    contentId = CHECKPOINT_ADD_MARKER,
                    position = initLatlng
                )
            )
            _driveScreenState.update {
                it.copy(
                    bottomSheetState = it.bottomSheetState.copy(
                        content = DriveBottomSheetContent.CHECKPOINT_ADD
                    ),
                    stateMode = DriveVisibleMode.BottomSheetExpand
                ).initCheckPointAddState(initLatlng)
            }
        }.onFailure { handleError(it) }
    }

    private suspend fun infoFloatingButtonClick(content: DriveBottomSheetContent) {
        _driveScreenState.update {
            val mode = when (content) {
                DriveBottomSheetContent.CHECKPOINT_INFO -> DriveVisibleMode.BlurCheckpointBottomSheetExpand
                else -> DriveVisibleMode.BlurBottomSheetExpand
            }
            it.run {
                copy(
                    popUpState = popUpState.copy(
                        commentState = popUpState.commentState.copy(
                            isContentVisible = false
                        )
                    ),
                    bottomSheetState = bottomSheetState.copy(
                        content = content,
                    ),
                    floatingButtonState = floatingButtonState.copy(
                        stateMode = DriveFloatingVisibleMode.Hide
                    ),
                    stateMode = mode,
                ).run {
                    when (bottomSheetState.content) {
                        DriveBottomSheetContent.COURSE_INFO -> {
                            val courseItem= mapContentRepository.selectedCourseState.value
                            initInfoState(course = courseItem?.course)
                        }
                        DriveBottomSheetContent.CHECKPOINT_INFO -> {
                            val checkPoint= mapContentRepository.selectedCheckPointState.value
                            initInfoState(checkPoint = checkPoint)
                        }
                        else -> this
                    }
                }
            }
        }
        _driveEvent.stopCameraWhenSheetChange()
    }

    private suspend fun exportMapFloatingButtonClick() {
        val floatingMode = _driveScreenState.value.floatingButtonState.stateMode
        if (floatingMode == DriveFloatingVisibleMode.ExportExpand) {
            _driveScreenState.update {
                it.copy(
                    floatingButtonState = it.floatingButtonState.copy(
                        stateMode = DriveFloatingVisibleMode.Default,
                        adItemGroup = emptyList()
                    ),
                    stateMode = DriveVisibleMode.CourseDetail
                )
            }
        } else {
            _driveScreenState.update {
                it.run {
                    copy(
                        floatingButtonState = floatingButtonState.copy(
                            stateMode = DriveFloatingVisibleMode.ExportExpand
                        ),
                        stateMode = DriveVisibleMode.BlurCourseDetail
                    )
                }
            }

            viewModelScope.launch(Dispatchers.IO) {
                delay(50)
                loadAdOnExportMapAppFloatExpand()
            }

            driveTutorialUseCase(DriveTutorialStep.EXPORT_FLOAT_CLICK)
        }
    }

    private suspend fun exportMapAppButtonClick(result: Result<Unit>) {
        result.onFailure {
            handleError(it)
        }
    }

    private suspend fun foldFloatingButtonClick() {
        _driveEvent.emit(DriveEvent.Release)
    }


    // 바텀 시트
    private suspend fun bottomSheetChange(state: SheetVisibleMode) {

        val driveState = _driveScreenState.value
        val content = driveState.bottomSheetState.content

        when (state) {
            SheetVisibleMode.Opening -> {}

            SheetVisibleMode.Opened -> handleBottomSheetOpened(content)

            SheetVisibleMode.Closing -> handleBottomSheetClosing(content, driveState)

            SheetVisibleMode.Closed -> handleBottomSheetClosed(content, driveState)
        }
    }

    private suspend fun handleBottomSheetOpened(
        content: DriveBottomSheetContent
    ) {
        when (content) {
            DriveBottomSheetContent.CHECKPOINT_ADD -> {
                _driveEvent.emit(
                    DriveEvent.MoveCamera(
                        MoveCameraOption(
                            trigger = CameraMoveTrigger.BOTTOM_SHEET_UP,
                            targetId = DefaultMapId.SELECT_COURSE_ID.name
                        )
                    )
                )
            }

            else -> {}
        }
    }

    private fun handleBottomSheetClosing(
        content: DriveBottomSheetContent,
        driveState: DriveScreenState
    ) {
        val isSheetVisible = DriveScreenState.bottomSheetVisible.contains(driveState.stateMode)
        val newClosingState = when (content) {
            DriveBottomSheetContent.COURSE_INFO -> {
                if (isSheetVisible) {
                    driveState.backToCourseDetail()
                } else null
            }

            DriveBottomSheetContent.CHECKPOINT_INFO -> {
                if (isSheetVisible) {
                    driveState.backToCheckPointPopUp()
                } else null
            }

            else -> {
                if (driveState.popUpState.commentState.isImeVisible) {
                    driveState.copy(
                        popUpState = driveState.popUpState.copy(
                            commentState = driveState.popUpState.commentState.copy(
                                isImeVisible = false
                            )
                        )
                    )
                } else null
            }
        } ?: driveState

        _driveScreenState.update { newClosingState }
    }

    private suspend fun handleBottomSheetClosed(
        content: DriveBottomSheetContent,
        driveState: DriveScreenState
    ) {

        val isSheetVisible = DriveScreenState.bottomSheetVisible.contains(driveState.stateMode)
        val isCommentContentVisible = driveState.popUpState.commentState.isContentVisible

        val newClosedState = when (content) {
            DriveBottomSheetContent.CHECKPOINT_ADD -> {
                if (isSheetVisible) {
                    _driveEvent.deleteMarker(CHECKPOINT_ADD_MARKER)
                    _driveEvent.emit(
                        DriveEvent.MoveCamera(
                            MoveCameraOption(
                                targetId = DefaultMapId.SELECT_COURSE_ID.name,
                                trigger = CameraMoveTrigger.BOTTOM_SHEET_DOWN,
                                animation = MoveAnimation.APP_EASING
                            )
                        )
                    )

                    driveState.backToCourseDetail()
                } else null
            }

            else -> {
                if (isCommentContentVisible) {
                    driveState.backToCheckPointPopUp()
                } else null
            }

        } ?: driveState

        _driveScreenState.update { newClosedState }

        driveTutorialUseCase(DriveTutorialStep.COMMENT_SHEET_DRAG)
    }

    private suspend fun checkpointLocationSliderChange(percent: Float) {
        runCatching {
            val points = mapContentRepository.selectedCourseState.value?.course?.points?:emptyList()
            points.getByPercent(percent)
        }.onSuccess { newLatlng ->
            _driveEvent.updateMarker(
                MarkerInfo(
                    contentId = CHECKPOINT_ADD_MARKER,
                    type = MarkerType.DEFAULT,
                    position = newLatlng
                )
            )
            _driveScreenState.update {
                it.copy(
                    bottomSheetState = it.bottomSheetState.copy(
                        checkPointAddState = it.bottomSheetState.checkPointAddState.copy(
                            latLng = newLatlng,
                            sliderPercent = percent
                        )
                    )
                )
            }
        }.onFailure {
            handleError(it)
        }
    }

    private fun List<LatLng>.getByPercent(percent: Float): LatLng {
        val points = this
        val index = when (percent) {
            100.0f -> points.size - 1
            0.0f -> 0
            else -> round((points.size - 1) * percent).toInt()
        }
        return points[index]
    }

    private fun checkpointDescriptionEnterClick(text: String) {
        _driveScreenState.update {
            val newCheckPointAddState = it.bottomSheetState
                .checkPointAddState.copy(description = text)
            val isValidateAddCheckPoint = newCheckPointAddState
                    .isValidateAddCheckPoint().isSuccess

            it.copy(
                bottomSheetState = it.bottomSheetState.copy(
                    checkPointAddState = newCheckPointAddState.copy(
                        isSubmitActive = isValidateAddCheckPoint
                    )
                )
            )
        }
    }

    private fun checkpointImageChange(imageInfo: ImageInfo) {
        _driveScreenState.update {
            val newCheckPointAddState = it.bottomSheetState.checkPointAddState.copy(
                imgUriString = imageInfo.uriString,
                imgInfo = imageInfo
            )
            val isValidateAddCheckPoint = newCheckPointAddState
                .isValidateAddCheckPoint().isSuccess

            it.copy(
                bottomSheetState = it.bottomSheetState.copy(
                    checkPointAddState = newCheckPointAddState.copy(
                        isSubmitActive = isValidateAddCheckPoint
                    )
                )
            )
        }
    }

    private suspend fun checkpointSubmitClick() {
        if (!_driveScreenState.value.bottomSheetState.checkPointAddState.isSubmitActive)
            return
        val content = _driveScreenState.value.bottomSheetState.checkPointAddState
            .toContent(DefaultMapId.SELECT_COURSE_ID.name)
        _driveScreenState.executeActionWithUpdateUi(
            loading = { state, isLoading -> state.replaceCheckpointAddLoading(isLoading) },
            action = { addCheckpointToCourseUseCase(content) },
            onSuccess = { newCheckPoint ->
                handler.handle(DriveMsgEvent.ADD_DONE)
                _driveEvent.addLeaf(newCheckPoint)
            },
            onSuccessUi = { it.backToCourseDetail() },
            isSuccessUiUpdateFirst = false
        )
    }

    private suspend fun infoReportClick(reason: ReportReason) =
        executeActionByContent(
            content = _driveScreenState.value.bottomSheetState.content,
            courseAction = { reportContentUseCase.bySelect(ReportType.COURSE, reason) },
            checkpointAction = { reportContentUseCase.bySelect(ReportType.CHECKPOINT, reason) },
            courseOnSuccess = { handler.handle(DriveMsgEvent.REPORT_DONE) },
            checkpointOnSuccess = { handler.handle(DriveMsgEvent.REPORT_DONE) }
        )

    private suspend fun infoRemoveClick() =
        executeActionByContent(
            content = _driveScreenState.value.bottomSheetState.content,
            courseAction = { removeCourseUseCase(DefaultMapId.SELECT_COURSE_ID.name) },
            checkpointAction = { removeCheckPointUseCase.bySelect() },
            courseOnSuccess = { handler.handle(DriveMsgEvent.REMOVE_DONE) },
            checkpointOnSuccess = { handler.handle(DriveMsgEvent.REMOVE_DONE) }
        )

    private suspend fun executeActionByContent(
        content: DriveBottomSheetContent,
        courseAction: suspend () -> Result<Any>,
        checkpointAction: suspend () -> Result<Any>,
        courseOnSuccess: suspend () -> Unit,
        checkpointOnSuccess: suspend () -> Unit,
        onError: suspend (Throwable) -> Unit = { handleError(it) }
    ) {
        _driveScreenState.update { it.replaceInfoLoading(true) }
        withContext(Dispatchers.IO) {
            when (content) {
                DriveBottomSheetContent.COURSE_INFO -> courseAction()
                DriveBottomSheetContent.CHECKPOINT_INFO -> checkpointAction()
                else -> Result.failure(IllegalStateException("Invalid content: $content"))
            }
        }.onSuccess {
            when (content) {
                DriveBottomSheetContent.COURSE_INFO -> {
                    courseOnSuccess()
                    _driveEvent.deleteSelectCourse()
                    _driveEvent.refreshCourses()
                }
                DriveBottomSheetContent.CHECKPOINT_INFO -> {
                    checkpointOnSuccess()
                    _driveEvent.deleteSelectCheckPoint()
                }
                else -> {}
            }
        }.onFailure {
            _driveScreenState.update { it.replaceInfoLoading(false) }
            onError(it)
        }
    }

    private suspend fun refreshCommentList(
        checkPointId: String,
        delay: Long = 0,
        withUiUpdate: Boolean = true
    ): List<Comment> {
        return withContext(Dispatchers.IO) { getCommentForCheckPointUseCase(checkPointId) }
            .onSuccess { originalGroup ->
                delay(delay) // 바텀시트 에니메이션 대기
                val itemGroup = originalGroup
                    .filter { !it.isHide }
                    .map { it.toItemState() }
                if (withUiUpdate)
                    _driveScreenState.update {
                        it.run {
                            if (popUpState.commentState.isContentVisible)
                                copy(
                                    popUpState = popUpState.copy(
                                        commentState = popUpState.commentState.copy(
                                            commentItemGroup = itemGroup
                                        )
                                    )
                                )
                            else this
                        }
                    }
            }.onFailure {
                handleError(it)
            }.getOrNull() ?: emptyList()
    }

    private fun DriveScreenState.updateListItem(courseGroup: List<Course>): DriveScreenState {
        val new = if (courseGroup.isEmpty()) emptyList() else courseGroup.map {
            ListState.ListItemState(course = it)
        }
        return copy(
            listState = listState.copy(
                listItemGroup = new
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
        if(step == DriveTutorialStep.DRIVE_GUIDE_DONE)
            driveTutorialUseCase(step)
    }

    private fun setTutorialStepUi(step: DriveTutorialStep) {
        viewModelScope.launch(dispatcher) {
            when (step) {
                DriveTutorialStep.MOVE_TO_COURSE -> {
                    _driveEvent.emit(
                        DriveEvent.MoveCamera(
                            MoveCameraOption(
                                latlng = guideCourse.cameraLatLng,
                                zoom = ZOOM.DISTRICT.level,
                                trigger = CameraMoveTrigger.GUIDE
                            )
                        )
                    )

                }

                DriveTutorialStep.SKIP -> {
                    _driveEvent.emit(
                        DriveEvent.MoveCamera(
                            MoveCameraOption(
                                zoom = ZOOM.PROVINCE.level - 0.1,
                                trigger = CameraMoveTrigger.GUIDE,
                                isMyLocation = true
                            )
                        )
                    )
                }

                else -> {}
            }
        }

        _driveScreenState.update { it.toStepState(step) }
    }


    //공통
    private fun lifecycleChange(event: AppLifecycle) {
        when (event) {

            AppLifecycle.onResume -> {
                loadAdWhenVisible()
            }

            AppLifecycle.onPause -> {
                clearAd()
            }

            else -> {}
        }
    }

    private suspend fun eventReceive(event: AppEvent, result: Boolean) {
        when (event) {
            AppEvent.SignInScreen -> {
                if (result) {
                    clearScreen()
                }
            }

            else -> {}
        }
    }

    private fun blurClick() {
        _driveScreenState.update {
            when (it.stateMode) {
                DriveVisibleMode.BlurCheckpointBottomSheetExpand -> it.backToCheckPointPopUp()
                DriveVisibleMode.BlurBottomSheetExpand,
                DriveVisibleMode.BlurCheckpointDetail,
                DriveVisibleMode.BlurCourseDetail -> {
                    mapContentRepository.clearCheckPoint()
                    it
                }

                else -> it
            }
        }
    }


    //유틸
    private fun loadAdWhenVisible(){
        val state = _driveScreenState.value
        when{
            state.stateMode == DriveVisibleMode.SearchBarExpand->{
                loadAdOnSearchBarExpand()
            }
            state.floatingButtonState.stateMode == DriveFloatingVisibleMode.ExportExpand ->{
                loadAdOnExportMapAppFloatExpand()
            }
        }
    }

    private fun loadAdOnSearchBarExpand() {
        val state = _driveScreenState.value
        viewModelScope.launch(Dispatchers.IO) {
            nativeAdService.getAd()
                .onSuccess { newAdItemGroup ->
                    if (state.stateMode == DriveVisibleMode.SearchBarExpand)
                        _driveScreenState.update {
                            it.copy(searchBarState = it.searchBarState.copy(adItemGroup = newAdItemGroup))
                        }
                }.onFailure {
                    handleError(it)
                }
        }
    }

    private fun loadAdOnExportMapAppFloatExpand(){
        val state = _driveScreenState.value
        viewModelScope.launch(Dispatchers.IO) {
            nativeAdService.getAd()
                .onSuccess { newAdItemGroup ->
                    if(state.floatingButtonState.stateMode == DriveFloatingVisibleMode.ExportExpand)
                        _driveScreenState.update {
                            it.copy(
                                floatingButtonState = it.floatingButtonState.copy(
                                    adItemGroup = newAdItemGroup
                                )
                            )
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
                popUpState.commentState.commentItemGroup?.map {
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

    private fun DriveScreenState.backToCourseDetail(): DriveScreenState {
        if(stateMode == DriveVisibleMode.Explorer) return this // 중복 요청 방지
        return copy(
            floatingButtonState = floatingButtonState.copy(
                stateMode = DriveFloatingVisibleMode.Default
            ),
            popUpState = PopUpState(),
            bottomSheetState = BottomSheetState(),
            stateMode = DriveVisibleMode.CourseDetail
        )
    }

    private fun DriveScreenState.backToCheckPointPopUp(): DriveScreenState {
        if(stateMode == DriveVisibleMode.Explorer) return this // 중복 요청 방지
        return copy(
            floatingButtonState = floatingButtonState.copy(
                stateMode = DriveFloatingVisibleMode.Popup
            ),
            bottomSheetState = BottomSheetState(),
            popUpState = popUpState.copy(
                commentState = CommentState(),
            ),
            stateMode = DriveVisibleMode.BlurCheckpointDetail
        )
    }

    private fun DriveScreenState.backToExplorer(): DriveScreenState {
        if(stateMode == DriveVisibleMode.Explorer) return this // 중복 요청 방지
        return copy(
                stateMode = DriveVisibleMode.Explorer,
                searchBarState = SearchBarState(),
                popUpState = PopUpState(),
                bottomSheetState = BottomSheetState(),
                floatingButtonState = FloatingButtonState()
            )
    }

    private suspend fun clearScreen() {
        _driveEvent.emit(DriveEvent.ClearMap)
        _driveScreenState.update {
            stateInit
        }
    }

    // ======================== 디버그
    private suspend fun debugOverlayClick() {
        clearCacheUseCase(setOf(AppCache.HISTORY))
        clearScreen()
        //signOutUseCase()
        handler.handle(DriveMsgEvent.UNKNOWN_ERR)
    }
}