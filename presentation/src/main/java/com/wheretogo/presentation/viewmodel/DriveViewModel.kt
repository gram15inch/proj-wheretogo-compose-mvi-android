package com.wheretogo.presentation.viewmodel

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheretogo.domain.LIST_ITEM_ZOOM
import com.wheretogo.domain.UseCaseFailType
import com.wheretogo.domain.getFocusComment
import com.wheretogo.domain.model.UseCaseResponse
import com.wheretogo.domain.model.map.CheckPoint
import com.wheretogo.domain.model.map.CheckPointAddRequest
import com.wheretogo.domain.model.map.Comment
import com.wheretogo.domain.model.map.Course
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.usecase.community.AddCommentToCheckPointUseCase
import com.wheretogo.domain.usecase.community.GetCommentForCheckPointUseCase
import com.wheretogo.domain.usecase.community.GetImageInfoUseCase
import com.wheretogo.domain.usecase.community.RemoveCheckPointUseCase
import com.wheretogo.domain.usecase.community.RemoveCommentToCheckPointUseCase
import com.wheretogo.domain.usecase.community.RemoveCourseUseCase
import com.wheretogo.domain.usecase.community.ReportCheckPointUseCase
import com.wheretogo.domain.usecase.community.ReportCommentUseCase
import com.wheretogo.domain.usecase.community.ReportCourseUseCase
import com.wheretogo.domain.usecase.community.UpdateLikeUseCase
import com.wheretogo.domain.usecase.map.AddCheckpointToCourseUseCase
import com.wheretogo.domain.usecase.map.GetCheckpointForMarkerUseCase
import com.wheretogo.domain.usecase.map.GetImageForPopupUseCase
import com.wheretogo.domain.usecase.map.GetNearByCourseUseCase
import com.wheretogo.domain.usecase.map.SearchKeywordUseCase
import com.wheretogo.domain.usecase.user.GetHistoryStreamUseCase
import com.wheretogo.domain.usecase.user.GetUserProfileStreamUseCase
import com.wheretogo.presentation.AppError
import com.wheretogo.presentation.AppEvent
import com.wheretogo.presentation.AppLifecycle
import com.wheretogo.presentation.CHECKPOINT_ADD_MARKER
import com.wheretogo.presentation.CLEAR_ADDRESS
import com.wheretogo.presentation.CameraUpdateSource
import com.wheretogo.presentation.CommentType
import com.wheretogo.presentation.DRIVE_LIST_MIN_ZOOM
import com.wheretogo.presentation.DriveBottomSheetContent
import com.wheretogo.presentation.MarkerType
import com.wheretogo.presentation.R
import com.wheretogo.presentation.SEARCH_MARKER
import com.wheretogo.presentation.SheetState
import com.wheretogo.presentation.feature.EventBus
import com.wheretogo.presentation.feature.ads.AdService
import com.wheretogo.presentation.feature.geo.distanceTo
import com.wheretogo.presentation.feature.map.DriveMapOverlayService
import com.wheretogo.presentation.getCommentEmogiGroup
import com.wheretogo.presentation.intent.DriveScreenIntent
import com.wheretogo.presentation.model.EventMsg
import com.wheretogo.presentation.model.MapOverlay
import com.wheretogo.presentation.model.MarkerInfo
import com.wheretogo.presentation.model.SearchBarItem
import com.wheretogo.presentation.state.BottomSheetState
import com.wheretogo.presentation.state.CameraState
import com.wheretogo.presentation.state.CheckPointAddState
import com.wheretogo.presentation.state.CommentState
import com.wheretogo.presentation.state.CommentState.CommentAddState
import com.wheretogo.presentation.state.CommentState.CommentItemState
import com.wheretogo.presentation.state.DriveScreenState
import com.wheretogo.presentation.state.FloatingButtonState
import com.wheretogo.presentation.state.InfoState
import com.wheretogo.presentation.state.ListState
import com.wheretogo.presentation.state.PopUpState
import com.wheretogo.presentation.state.SearchBarState
import com.wheretogo.presentation.toComment
import com.wheretogo.presentation.toDomainLatLng
import com.wheretogo.presentation.toItem
import com.wheretogo.presentation.toSearchBarItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.round

@HiltViewModel
class DriveViewModel @Inject constructor(
    private val getNearByCourseUseCase: GetNearByCourseUseCase,
    private val getCommentForCheckPointUseCase: GetCommentForCheckPointUseCase,
    private val getCheckPointForMarkerUseCase: GetCheckpointForMarkerUseCase,
    private val getHistoryStreamUseCase: GetHistoryStreamUseCase,
    private val getUserProfileStreamUseCase: GetUserProfileStreamUseCase,
    private val getImageForPopupUseCase: GetImageForPopupUseCase,
    private val getImageInfoUseCase: GetImageInfoUseCase,
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
    private val mapOverlayService: DriveMapOverlayService,
    private val nativeAdService: AdService
) : ViewModel() {
    private val _driveScreenState =
        MutableStateFlow(
            DriveScreenState(
                overlayGroup = mapOverlayService.overlays,
                searchBarState = SearchBarState(
                    isAdVisible = true
                )
            )
        )
    val driveScreenState: StateFlow<DriveScreenState> = _driveScreenState
    private var isMapUpdate = true

    override fun onCleared() {
        super.onCleared()
        _driveScreenState.value.destroyAd()
    }

    fun handleIntent(intent: DriveScreenIntent) {
        viewModelScope.launch {
            when (intent) {
                //서치바
                is DriveScreenIntent.AddressItemClick -> searchBarItemClick(intent.searchBarItem)
                is DriveScreenIntent.SearchBarClick -> searchBarClick()
                is DriveScreenIntent.SearchBarClose -> searchBarClose()
                is DriveScreenIntent.SearchSubmit -> searchSubmit(intent.submit)

                //지도
                is DriveScreenIntent.MapIsReady -> mapIsReady()
                is DriveScreenIntent.CameraUpdated -> cameraUpdated(intent.cameraState)
                is DriveScreenIntent.CourseMarkerClick -> courseMarkerClick(intent.overlay)
                is DriveScreenIntent.CheckPointMarkerClick -> checkPointMarkerClick(intent.overlay)
                is DriveScreenIntent.ContentPaddingChanged -> contentPaddingChanged(intent.amount)

                //목록
                is DriveScreenIntent.DriveListItemClick ->  driveListItemClick(intent.itemState)

                //팝업
                is DriveScreenIntent.DismissPopupComment -> dismissPopupComment()
                is DriveScreenIntent.CommentListItemClick -> commentListItemClick(intent.itemState)
                is DriveScreenIntent.CommentListItemLongClick -> commentListItemLongClick(intent.itemState)
                is DriveScreenIntent.CommentLikeClick -> commentLikeClick(intent.itemState)
                is DriveScreenIntent.CommentAddClick -> commentAddClick(intent.itemState)
                is DriveScreenIntent.CommentRemoveClick -> commentRemoveClick(intent.itemState)
                is DriveScreenIntent.CommentReportClick -> commentReportClick(intent.itemState)
                is DriveScreenIntent.CommentEditValueChange -> commentEditValueChange(intent.textFiled)
                is DriveScreenIntent.CommentEmogiPress -> commentEmogiPress(intent.emogi)
                is DriveScreenIntent.CommentTypePress -> commentTypePress(intent.type)

                //플로팅
                is DriveScreenIntent.CommentFloatingButtonClick -> commentFloatingButtonClick()
                is DriveScreenIntent.CheckpointAddFloatingButtonClick -> checkpointAddFloatingButtonClick()
                is DriveScreenIntent.InfoFloatingButtonClick -> infoFloatingButtonClick()
                is DriveScreenIntent.ExportMapFloatingButtonClick -> exportMapFloatingButtonClick()
                is DriveScreenIntent.ExportMapAppButtonClick -> exportMapAppButtonClick(intent.result)
                is DriveScreenIntent.FoldFloatingButtonClick -> foldFloatingButtonClick()

                //바텀시트
                is DriveScreenIntent.BottomSheetChange -> bottomSheetChange(intent.state)
                is DriveScreenIntent.CheckpointLocationSliderChange -> checkpointLocationSliderChange(intent.percent)
                is DriveScreenIntent.CheckpointDescriptionChange -> checkpointDescriptionChange(intent.text)
                is DriveScreenIntent.CheckpointDescriptionEnterClick -> checkpointDescriptionEnterClick()
                is DriveScreenIntent.CheckpointImageChange -> checkpointImageChange(intent.imgUri)
                is DriveScreenIntent.CheckpointSubmitClick -> checkpointSubmitClick()
                is DriveScreenIntent.InfoReportClick -> infoReportClick(intent.infoState)
                is DriveScreenIntent.InfoRemoveClick -> infoRemoveClick(intent.infoState)

                //공통
                is DriveScreenIntent.LifecycleChange -> lifecycleChange(intent.event)
                is DriveScreenIntent.BlurClick -> blurClick()

            }
        }
    }

    //서치바
    private fun searchBarItemClick(item: SearchBarItem) {
        if(item.label != CLEAR_ADDRESS && item.latlng!=null){

            _driveScreenState.update {
                mapOverlayService.removeCheckPoint(listOf(SEARCH_MARKER))
                mapOverlayService.addCheckPoint(listOf(CheckPoint(SEARCH_MARKER, latLng = item.latlng)))
                    it.run {
                        copy(
                            searchBarState = searchBarState.copy(isLoading = false),
                            naverMapState = naverMapState.copy(
                                cameraState = naverMapState.cameraState.copy(
                                    latLng = item.latlng,
                                    updateSource = CameraUpdateSource.APP_EASING
                                )
                            )
                        )
                    }

            }
        } else {
            mapOverlayService.removeCheckPoint(listOf(SEARCH_MARKER))
            _driveScreenState.update { it.searchBarInit() }
        }

    }

    private fun searchBarClick() {
        mapOverlayService.removeCheckPoint(listOf(SEARCH_MARKER))
        _driveScreenState.update {
            it.run{
                copy(
                    searchBarState = searchBarState.copy(isActive = true, searchBarItemGroup = emptyList())
                )
            }

        }

        if(_driveScreenState.value.searchBarState.adItemGroup.isEmpty()){
            _driveScreenState.update {
                it.run {
                    copy(
                        listState = listState.copy(isVisible = false))

                }
            }
           loadAd()
        } else {
            searchBarClose()
        }
    }

    private fun searchBarClose(){
        mapOverlayService.removeCheckPoint(listOf(SEARCH_MARKER))
        _driveScreenState.update {
            it.searchBarInit().copy(
                listState = it.listState.copy(isVisible = true)
            )
        }
    }

    private suspend fun searchSubmit(address: String) {
        if(address.trim().isNotBlank()){ // 주소 없을시 취소 버튼
            _driveScreenState.update { it.copy(searchBarState = it.searchBarState.copy(isLoading = true)) }
            val keywordResponse = withContext(Dispatchers.IO) { searchKeywordUseCase(address) }
            _driveScreenState.update {
                it.run {
                    when (keywordResponse.status) {
                        UseCaseResponse.Status.Success -> {
                            copy(
                                searchBarState = searchBarState.copy(
                                    isLoading = false,
                                    isEmptyVisible = keywordResponse.data?.isEmpty() ?: false,
                                    searchBarItemGroup = keywordResponse.data?.map { it.toSearchBarItem() } ?: emptyList()
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
        } else {
            _driveScreenState.update { it.searchBarInit() }
        }

    }

    private fun DriveScreenState.searchBarInit(): DriveScreenState {
        clearAd()
        return copy(
            searchBarState = searchBarState.copy(
                isActive = false,
                isLoading = false,
                isEmptyVisible = false,
                searchBarItemGroup = emptyList(),
                adItemGroup = emptyList()
            )
        )
    }



    //지도
    private fun mapIsReady() {
        _driveScreenState.update {
            it.copy(naverMapState = it.naverMapState.copy(isMapReady = true))
        }
    }

    private suspend fun cameraUpdated(cameraState: CameraState) {
        val isCameraUpdate =
            _driveScreenState.value.run {
                val oldCamera = naverMapState.cameraState
                oldCamera.latLng.distanceTo(cameraState.latLng) >= 1
                        || oldCamera.updateSource != cameraState.updateSource
            }

        val isContentsUpdate = _driveScreenState.value.run {
            isCameraUpdate &&
                    !bottomSheetState.isVisible &&
                    !floatingButtonState.isFoldVisible && isMapUpdate
        }

        if (isCameraUpdate)
            _driveScreenState.update {
                it.copy(
                    naverMapState = it.naverMapState.copy(
                        cameraState = cameraState.copy(updateSource = CameraUpdateSource.USER)
                    )
                )
            }

        if (isContentsUpdate) {
            isMapUpdate = false
            _driveScreenState.updateLoading(true)
            _driveScreenState.update { it.updateNearCourse(cameraState) }
            isMapUpdate = true
            _driveScreenState.updateLoading(false)
        }

    }

    private fun courseMarkerClick(overlay: MapOverlay.MarkerContainer) {

        _driveScreenState.update {
            it.run {
                val zoom =
                    if (naverMapState.cameraState.zoom > DRIVE_LIST_MIN_ZOOM) naverMapState.cameraState.zoom else DRIVE_LIST_MIN_ZOOM + 0.1
                val latlng = overlay.marker.position.toDomainLatLng()
                val newCameraState = CameraState(latlng, zoom, updateSource = CameraUpdateSource.APP_EASING)
                copy(
                    naverMapState = naverMapState.copy(cameraState = newCameraState)
                )
            }

        }

    }

    private suspend fun checkPointMarkerClick(overlay: MapOverlay.MarkerContainer) {
        _driveScreenState.value.apply {
            if(listState.clickItem.course.courseId.isBlank()){
                mapOverlayService.clearCheckPoint()
                return
            }
            if(bottomSheetState.isVisible)
                return
        }

        _driveScreenState.update {
            it.initWithLevelState(3).run {
                copy(
                    popUpState = popUpState.copy(
                        isVisible = true,
                        checkPointId = overlay.id
                    ),
                    bottomSheetState = bottomSheetState.copy(
                        infoState = bottomSheetState.infoState.copy(
                            isCourseInfo = false
                        )
                    )
                )
            }
        }

        val checkpoint = withContext(Dispatchers.IO) {
            _driveScreenState.value.run {
                val course = listState.clickItem.course
                getCheckPointForMarkerUseCase(course.courseId).firstOrNull { it.checkPointId == overlay.id }
            }
        }

        if(checkpoint==null){
            mapOverlayService.removeCheckPoint(listOf(overlay.id))
            return
        }

        _driveScreenState.update {
            it.run {
                if (popUpState.isVisible)
                    copy(
                        bottomSheetState = bottomSheetState.copy(
                            infoState = bottomSheetState.infoState.copy(
                                checkPoint = checkpoint
                            )
                        )
                    )
                else this
            }

        }
        val image =
            withContext(Dispatchers.IO) { getImageForPopupUseCase(checkpoint.imageName) }
        _driveScreenState.update {
            it.run{
                if (popUpState.isVisible)
                    copy(
                        popUpState = popUpState.copy(
                            checkPointId = checkpoint.checkPointId,
                            imageUri = image
                        )
                    )
                else this
            }

        }
    }

    private suspend fun contentPaddingChanged(amount:Int){}



    private fun moveCamera(latLng: LatLng, zoom:Double? = null){
        if(latLng!=LatLng())
            _driveScreenState.update {
                it.run {
                    val oldZoom = zoom ?: naverMapState.cameraState.zoom
                    val newZoom = when {
                        oldZoom <= LIST_ITEM_ZOOM -> LIST_ITEM_ZOOM
                        else-> oldZoom
                    }

                    copy(
                        naverMapState = naverMapState.copy(
                            cameraState = naverMapState.cameraState.copy(
                                latLng = latLng,
                                zoom = newZoom,
                                updateSource = CameraUpdateSource.APP_LINEAR
                            )
                        )
                    )
                }

            }
    }

    //목록
    private suspend fun driveListItemClick(state: ListState.ListItemState) {
        val course = state.course
        _driveScreenState.update {
            it.initWithLevelState(2).run {
                copy(
                    isLoading = true,
                    listState = listState.copy(
                        clickItem = state
                    ),
                    bottomSheetState = bottomSheetState.copy(
                        infoState = bottomSheetState.infoState.copy(
                            course = course
                        )

                    )
                )
            }

        }
        moveCamera(course.cameraLatLng)
        val checkPointGroup =
            withContext(Dispatchers.IO) { getCheckPointForMarkerUseCase(course.courseId) }
        _driveScreenState.update {
            val currentCourse = it.listState.clickItem.course
            if(state.course.courseId == currentCourse.courseId) {
                mapOverlayService.focusCourse(course)
                mapOverlayService.addCheckPoint(checkPointGroup)
            }
            it.copy(isLoading = false)
        }

    }

    //팝업
    private fun blurClick() {
        _driveScreenState.update { it.initWithLevelState(2) }
    }

    private fun dismissPopupComment() {
        _driveScreenState.update { it.initWithLevelState(3) }
    }

    private fun commentListItemClick(itemState: CommentItemState) {
        _driveScreenState.update {
            it.run{
                copy(
                    popUpState = popUpState.copy(
                        commentState = popUpState.commentState.copy(
                            commentItemGroup = popUpState.commentState.commentItemGroup.map {
                                if (it.data.commentId == itemState.data.commentId && itemState.data.detailedReview.length > 10)
                                    it.copy(isFold = !it.isFold)
                                else
                                    it
                            }
                        )
                    )
                )
            }

        }
    }

    private fun commentListItemLongClick(itemState: CommentItemState) {
        val isCommentSettingVisible =
            _driveScreenState.value.popUpState.commentState.isCommentSettingVisible
        _driveScreenState.update {
            it.run {
                copy(
                    popUpState = popUpState.copy(
                        commentState = popUpState.commentState.copy(
                            isCommentSettingVisible = !isCommentSettingVisible,
                            selectedCommentSettingItem = itemState
                        ),
                    )
                )
            }

        }
    }

    private suspend fun commentLikeClick(itemState: CommentItemState) {
        likeSwitch(itemState.data.commentId, itemState.isLike)
        withContext(Dispatchers.IO) {
            updateLikeUseCase(comment = itemState.data, isLike = !itemState.isLike)
        }
        val commentItemGroup = getCommentItemGroupAndUpdateCaption(itemState.data.groupId)
        _driveScreenState.update {
            it.run {
                if(popUpState.isVisible)
                    copy(
                        popUpState = popUpState.copy(
                            commentState = popUpState.commentState.copy(
                                commentItemGroup = commentItemGroup
                            )
                        )
                    )
                else this
            }
        }
    }

    private suspend fun updateCheckPointCaption(checkPointId: String, caption: String) {
        withContext(Dispatchers.Main) {
            mapOverlayService.updateMarker(
                MarkerInfo(
                    contentId = checkPointId,
                    type = MarkerType.CHECKPOINT,
                    caption = caption
                )
            )
        }
    }

    private suspend fun commentAddClick(itemState: CommentAddState) {
        val comment = itemState.toComment()
        if(comment.oneLineReview.isBlank())
            return

        val response = withContext(Dispatchers.IO) { addCommentToCheckPointUseCase(comment) }
        when (response.status) {
            UseCaseResponse.Status.Success -> {
                val commentItemGroup = getCommentItemGroupAndUpdateCaption(itemState.groupId)
                _driveScreenState.update {
                    it.run {
                        copy(
                            popUpState = popUpState.copy(
                                commentState = popUpState.commentState.copy(
                                    commentAddState = CommentAddState(
                                        groupId = comment.groupId, emogiGroup = getCommentEmogiGroup()
                                    ),
                                    commentItemGroup = commentItemGroup
                                )
                            )
                        )
                    }

                }
            }

            else -> {
                when (response.failType) {
                    UseCaseFailType.INVALID_USER -> {
                    }

                    else -> {
                        _driveScreenState.update { it.initBottomSheet() }
                    }
                }
            }
        }
    }

    private suspend fun commentRemoveClick(itemState: CommentItemState) {
        withContext(Dispatchers.IO) { removeCommentToCheckPointUseCase(itemState.data) }
        val checkPointId = _driveScreenState.value.popUpState.checkPointId
        val commentItemGroup = getCommentItemGroupAndUpdateCaption(checkPointId)
        _driveScreenState.update {
            it.run {
                copy(
                    popUpState = popUpState.copy(
                        commentState = popUpState.commentState.copy(
                            isCommentSettingVisible = false,
                            selectedCommentSettingItem = CommentItemState(),
                            commentItemGroup = commentItemGroup
                        )
                    )
                )
            }

        }
    }

    private suspend fun commentReportClick(itemState: CommentItemState) {
        val reportResponse = withContext(Dispatchers.IO) { reportCommentUseCase(itemState.data) }
        when (reportResponse.status) {
            UseCaseResponse.Status.Success -> {
                val commentItemGroup = getCommentItemGroupAndUpdateCaption(itemState.data.groupId)
                _driveScreenState.update {
                    it.run {
                        copy(
                            popUpState = popUpState.copy(
                                commentState = popUpState.commentState.copy(
                                    commentItemGroup = commentItemGroup,
                                    isCommentSettingVisible = false,
                                    selectedCommentSettingItem = CommentItemState()
                                )
                            )
                        )
                    }

                }
            }

            UseCaseResponse.Status.Fail -> {}
        }

    }

    private fun commentEditValueChange(text: TextFieldValue) {
        _driveScreenState.update {
            it.run {
                copy(
                    popUpState = popUpState.copy(
                        commentState = popUpState.commentState.copy(
                            commentAddState = popUpState.commentState.commentAddState.copy(
                                editText = text
                            )
                        )
                    )
                )
            }

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

    private fun commentTypePress(type: CommentType) {
        _driveScreenState.update {
            it.run {
                val newCommentAddState = when (type) {
                    CommentType.ONE -> {
                        popUpState.commentState.commentAddState.copy(
                            commentType = type,
                            oneLineReview = "",
                            oneLinePreview = "",
                            detailReview = popUpState.commentState.commentAddState.editText.text,
                            editText = TextFieldValue(
                                text = popUpState.commentState.commentAddState.oneLineReview,
                                selection = TextRange(popUpState.commentState.commentAddState.oneLineReview.length)
                            ),
                            isLargeEmogi = true,
                            isEmogiGroup = true,
                        )
                    }

                    CommentType.DETAIL -> {
                        popUpState.commentState.commentAddState.copy(
                            commentType = type,
                            oneLineReview = popUpState.commentState.commentAddState.editText.text,
                            detailReview = "",
                            oneLinePreview = popUpState.commentState.commentAddState.run { "${largeEmoji.ifEmpty { emogiGroup.firstOrNull() ?: "" }}  ${editText.text}" },
                            editText = TextFieldValue(
                                text = popUpState.commentState.commentAddState.detailReview,
                                selection = TextRange(popUpState.commentState.commentAddState.detailReview.length)
                            ),
                            isLargeEmogi = false,
                            isEmogiGroup = false,
                        )
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
        if(_driveScreenState.value.popUpState.commentState.isCommentVisible){
            dismissPopupComment()
            return
        }

        val checkPointId = _driveScreenState.value.popUpState.checkPointId
        val emogiGroup = getCommentEmogiGroup()
        _driveScreenState.update {
            it.run {
                copy(
                    popUpState = popUpState.copy(
                        commentState = popUpState.commentState.copy(
                            isCommentVisible = !popUpState.commentState.isCommentVisible,
                            isLoading = true,
                            commentAddState = CommentAddState(
                                groupId = checkPointId,
                                largeEmoji = emogiGroup.firstOrNull() ?: "",
                                emogiGroup = emogiGroup
                            )
                        ),
                    ),
                    floatingButtonState = floatingButtonState.copy(isBackPlateVisible = false),
                    bottomSheetState = bottomSheetState.copy(isVisible = false)
                )
            }
        }

        val commentItemGroup = getCommentItemGroupAndUpdateCaption(checkPointId)
        delay(200) // 슬라이드 에니메이션중 버벅임 예방
        _driveScreenState.update {
            it.run {
                if (popUpState.commentState.isCommentVisible)
                    copy(
                        popUpState = popUpState.copy(
                            commentState = popUpState.commentState.copy(
                                isLoading = false,
                                commentItemGroup = commentItemGroup
                            )
                        )
                    )
                else this
            }

        }
    }

    private fun checkpointAddFloatingButtonClick() {
        val course = _driveScreenState.value.listState.clickItem.course
        mapOverlayService.addCheckPoint(
            listOf(
                CheckPoint(
                    checkPointId = CHECKPOINT_ADD_MARKER,
                    latLng = course.waypoints.first()
                )
            ),
        )
        _driveScreenState.update {
            it.checkPointAddStateInit()
                .copy(
                    bottomSheetState = it.bottomSheetState.copy(
                        isVisible = true,
                        content = DriveBottomSheetContent.CHECKPOINT_ADD,
                    )
                )
        }
    }

    private fun DriveScreenState.checkPointAddStateInit():DriveScreenState{
        val course = _driveScreenState.value.listState.clickItem.course
        val newCheckPointAddState = bottomSheetState.checkPointAddState.run {
            copy(
                latlng = course.waypoints.first(),
                sliderPercent = 0.0f,
            )
        }
       return copy(
            bottomSheetState = bottomSheetState.copy(
                checkPointAddState = newCheckPointAddState,
            )
        )
    }

    private suspend fun infoFloatingButtonClick() {
        _driveScreenState.update {
            it.run {
                infoStateInit()
                copy(
                    bottomSheetState = bottomSheetState.copy(
                        isVisible = true,
                        content = DriveBottomSheetContent.INFO,
                    ),
                    popUpState = popUpState.copy(
                        commentState = popUpState.commentState.copy(
                            isCommentVisible = false
                        )
                    )
                )
            }

        }
    }

    private suspend fun DriveScreenState.infoStateInit():DriveScreenState{
        val isMine = if (bottomSheetState.infoState.isCourseInfo) {
            bottomSheetState.infoState.course.isMine()
        } else {
            bottomSheetState.infoState.checkPoint.isMine()
        }
        val infoState = bottomSheetState.infoState.copy(
            course = listState.clickItem.course,
            isRemoveButton = isMine,
            isReportButton = true
        )
        return copy(
            bottomSheetState = bottomSheetState.copy(
                infoState = infoState
            )
        )
    }

    private fun exportMapFloatingButtonClick() {

        _driveScreenState.update {
            it.run {
                initWithLevelState(2).copy(
                    floatingButtonState = floatingButtonState.copy(
                        isBackPlateVisible = !floatingButtonState.isBackPlateVisible
                    )
                )
            }
        }

        if(_driveScreenState.value.floatingButtonState.isBackPlateVisible){
           viewModelScope.launch(Dispatchers.IO) {
               delay(50)
               loadAd()
           }
        } else {
            _driveScreenState.update {
                it.copy(
                    floatingButtonState = it.floatingButtonState.copy(
                        adItemGroup = emptyList()
                    )
                )
            }
        }
    }

    private suspend fun exportMapAppButtonClick(result: Result<Unit>) {
        result.onSuccess {
            _driveScreenState.update {
                it.run {
                    copy(
                        floatingButtonState = floatingButtonState.copy(
                            isBackPlateVisible = !floatingButtonState.isBackPlateVisible
                        )
                    )
                }

            }
        }.onFailure {
            when(it){
                is AppError.MapNotSupportExcludeLocation->
                    EventBus.send(AppEvent.SnackBar(EventMsg(R.string.no_supprot_app_exclude_my_loction)))
            }
        }
    }

    private fun foldFloatingButtonClick() {
        val course = _driveScreenState.value.listState.clickItem.course
        _driveScreenState.update {
            mapOverlayService.removeCheckPoint(course.checkpointIdGroup)
            mapOverlayService.showAll()
            it.initWithLevelState(1)
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private suspend fun bottomSheetChange(state:SheetState) {
        val content = _driveScreenState.value.bottomSheetState.content
            when(content){
                DriveBottomSheetContent.CHECKPOINT_ADD->{
                    val course = _driveScreenState.value.listState.clickItem.course
                    when(state){
                        SheetState.Expand->{
                            _driveScreenState.update { it. checkPointAddStateInit() }
                            mapOverlayService.addCheckPoint(
                                listOf(
                                    CheckPoint(
                                        checkPointId = CHECKPOINT_ADD_MARKER,
                                        latLng = course.waypoints.first()
                                    )
                                ),
                            )
                        }
                        SheetState.Expanded->{
                            moveCamera(course.cameraLatLng)
                        }
                        SheetState.PartiallyExpand->{
                            mapOverlayService.removeCheckPoint(listOf(CHECKPOINT_ADD_MARKER))
                            _driveScreenState.update {
                                it.copy(bottomSheetState = it.bottomSheetState.copy(isVisible = false))
                            }
                        }
                        SheetState.PartiallyExpanded->{
                            moveCamera(course.cameraLatLng)
                        }
                    }
                }
                DriveBottomSheetContent.INFO->{
                    when(state){
                        SheetState.Expand->{
                            _driveScreenState.value = _driveScreenState.value.infoStateInit()
                        }
                        SheetState.PartiallyExpand->{
                            _driveScreenState.update {
                                it.copy(bottomSheetState = it.bottomSheetState.copy(isVisible = false))
                            }
                        }
                        else->{}
                    }
                }
                else->{}
            }
    }

    //바텀시트
    private fun DriveScreenState.initBottomSheet(): DriveScreenState {
        return copy(
            bottomSheetState = bottomSheetState.copy(
                isVisible = false,
                checkPointAddState = CheckPointAddState()
            ),
        )
    }

    private fun checkpointLocationSliderChange(percent: Float) {
        _driveScreenState.update {
            val points = it.listState.clickItem.course.points
            val index = round((points.size - 1) * percent).toInt()
            val newLatlng = points[index]
            mapOverlayService.updateMarker(
                MarkerInfo(
                    contentId = CHECKPOINT_ADD_MARKER,
                    type = MarkerType.CHECKPOINT,
                    position = newLatlng
                )
            )
            val newCheckPointAddState = it.bottomSheetState.checkPointAddState.run {
                copy(
                    latlng = newLatlng,
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

    private suspend fun checkpointImageChange(imgUri: Uri?) {
        imgUri?.let {
            _driveScreenState.update{
                val newCheckPointAddState = it.bottomSheetState.checkPointAddState.run {
                    copy(
                        imgUri = imgUri,
                        imgInfo = withContext(Dispatchers.IO) { getImageInfoUseCase(imgUri) },
                        isSubmitActive = isValidateAddCheckPoint().isSuccess
                    )
                }
                it.copy(bottomSheetState = it.bottomSheetState.copy(checkPointAddState = newCheckPointAddState))
            }
        }
    }

    private suspend fun checkpointSubmitClick() {
        val course = _driveScreenState.value.listState.clickItem.course
        if (_driveScreenState.value.bottomSheetState.checkPointAddState.isSubmitActive) {
            _driveScreenState.updateLoading(true)
            val newCheckpointAddRequest =
                _driveScreenState.value.bottomSheetState.checkPointAddState.toRequest(course.courseId)
            val addCheckPointResponse =
                withContext(Dispatchers.IO) { addCheckpointToCourseUseCase(newCheckpointAddRequest) }

            when (addCheckPointResponse.status) {
                UseCaseResponse.Status.Success -> {
                    val newCheckPointGroup =
                        withContext(Dispatchers.IO) { getCheckPointForMarkerUseCase(course.courseId) }
                    val newCheckPoint =
                        newCheckPointGroup.filter { it.checkPointId == addCheckPointResponse.data }

                    _driveScreenState.update {
                        it.initWithLevelState(2)
                        .run {
                            mapOverlayService.addCheckPoint(newCheckPoint)
                            mapOverlayService.removeCheckPoint(listOf(CHECKPOINT_ADD_MARKER))
                            copy(
                                listState = listState.copy(
                                    clickItem = listState.clickItem.copy(
                                        course = course.copy(
                                            checkpointIdGroup = newCheckPointGroup.map { it.checkPointId })
                                    )
                                ),
                                bottomSheetState = bottomSheetState.copy(
                                    checkPointAddState = bottomSheetState.checkPointAddState.copy(
                                        isLoading = false
                                    )
                                )
                            )
                        }.initBottomSheet()
                    }
                }
                UseCaseResponse.Status.Fail ->{
                    _driveScreenState.update {
                        it.run {
                            copy(
                                bottomSheetState = bottomSheetState.copy(
                                    checkPointAddState = bottomSheetState.checkPointAddState.copy(
                                        isLoading = false
                                    )
                                )
                            )
                        }

                    }
                }
                else -> {}
            }
        }
    }

    private fun checkpointDescriptionEnterClick() {}

    private suspend fun infoRemoveClick(infoState: InfoState) {
        val course = infoState.course
        _driveScreenState.updateLoading(true)
        if (infoState.isCourseInfo) { // 코스
            val removeResponse =
                withContext(Dispatchers.IO) { removeCourseUseCase(course.courseId) }
            when (removeResponse.status) {
                UseCaseResponse.Status.Success -> {
                    _driveScreenState.update {
                        it.run {
                            mapOverlayService.removeCourse(listOf(course.courseId))
                            mapOverlayService.removeCheckPoint(course.checkpointIdGroup)
                            initWithLevelState(1)
                                .updateNearCourse(naverMapState.cameraState)
                        }
                    }
                }

                else -> {}
            }

        } else { // 체크포인트
            val checkPoint = infoState.checkPoint
            val removeResponse = withContext(Dispatchers.IO) {
                removeCheckPointUseCase(course.courseId, checkPoint.checkPointId)
            }
            when (removeResponse.status) {
                UseCaseResponse.Status.Success -> {
                    _driveScreenState.update {
                        mapOverlayService.removeCheckPoint(listOf(checkPoint.checkPointId))
                        it.initWithLevelState(2)
                    }
                }

                else -> {}
            }
        }
        _driveScreenState.updateLoading(false)
    }

    private suspend fun infoReportClick(infoState: InfoState) {
        val course = infoState.course

        _driveScreenState.updateLoading(true)
        if (infoState.isCourseInfo) { // 코스
            val reportResponse = withContext(Dispatchers.IO) {
                reportCourseUseCase(infoState.course, infoState.reason)
            }
            when (reportResponse.status) {
                UseCaseResponse.Status.Success -> {
                    _driveScreenState.update {
                        it.run {
                            mapOverlayService.removeCourse(listOf(course.courseId))
                            mapOverlayService.removeCheckPoint(course.checkpointIdGroup)
                            initWithLevelState(1)
                                .updateNearCourse(naverMapState.cameraState)
                        }
                    }
                }

                else -> {
                    _driveScreenState.updateLoading(false)
                }
            }

        } else { // 체크포인트
            val checkPointId = infoState.checkPoint.checkPointId
            val reportResponse = withContext(Dispatchers.IO) {
                reportCheckPointUseCase(checkPointId, "")
            }

            when (reportResponse.status) {
                UseCaseResponse.Status.Success -> {
                    _driveScreenState.update {
                        it.initWithLevelState(2).apply {
                            mapOverlayService.removeCheckPoint(listOf(checkPointId))
                        }
                    }
                }

                else -> {
                    _driveScreenState.updateLoading(false)
                }
            }
        }
    }

    private suspend fun DriveScreenState.updateNearCourse(cameraState: CameraState): DriveScreenState {
        val newCourseGroup = withContext(Dispatchers.IO) {
            getNearByCourseUseCase(cameraState.latLng, cameraState.zoom)
        }

        val listItemGroup = if (cameraState.zoom > DRIVE_LIST_MIN_ZOOM) {
            newCourseGroup.filterNearByListGroup(center = cameraState.latLng, meter = 3000)
        } else emptyList()

        return run {
            mapOverlayService.addCourse(newCourseGroup)
            mapOverlayService.showAll()
            copy(
                listState = listState.copy(
                    listItemGroup = listItemGroup
                )
            )
        }
    }

    private fun CheckPointAddState.isValidateAddCheckPoint(): Result<Unit> {
        return runCatching {
            require(this.imgUri != null) { AppError.ImgEmpty() }
            require(this.description.isNotEmpty()) { AppError.DescriptionEmpty() }
        }
    }

    private fun DriveScreenState.initWithLevelState(level: Int): DriveScreenState {
        clearAd()
        return when (level) {
            1 -> {//목록
                run {
                    copy(
                        searchBarState = searchBarState.copy(isVisible = true),
                        listState = listState.copy(
                            isVisible = true,
                            clickItem = ListState.ListItemState()
                        ),
                        popUpState = popUpState.copy(isVisible = false),
                        floatingButtonState = FloatingButtonState(),
                        bottomSheetState = BottomSheetState()
                    )
                }
            }

            2 -> {//코스
                run {
                    copy(
                        searchBarState = searchBarState.copy(isVisible = false),
                        listState = listState.copy(isVisible = false),
                        popUpState = PopUpState(isVisible = false),
                        floatingButtonState = FloatingButtonState(
                            emptyList(),
                            false, true, true, true, false, true
                        ),
                        bottomSheetState = bottomSheetState.copy(
                            isVisible = false,
                            infoState = bottomSheetState.infoState.copy(
                                isCourseInfo = true,
                                checkPoint = CheckPoint()
                            )
                        )
                    )
                }
            }

            3 -> {//체크포인트
                run {
                    copy(
                        searchBarState = searchBarState.copy(isVisible = false),
                        floatingButtonState = FloatingButtonState(
                            emptyList(),
                            true, false, true, true, false, true
                        ),
                        popUpState = popUpState.copy(
                            commentState = CommentState()
                        )
                    )
                }

            }

            4 -> {//바텀시트
                run {
                    copy(
                        searchBarState = searchBarState.copy(isVisible = false),
                        listState = listState.copy(isVisible = false),
                        popUpState = popUpState.copy(isVisible = false),
                        floatingButtonState = FloatingButtonState(),
                        bottomSheetState = BottomSheetState()
                    )
                }
            }

            else -> this
        }
    }

    //공통
    private fun lifecycleChange(event: AppLifecycle){
        when(event){

            AppLifecycle.onResume->{
                loadAd()
            }

            AppLifecycle.onPause->{
                clearAd()
            }

            AppLifecycle.onDispose->{
                clearAd()
            }

            else->{}
        }
    }

    //유틸
    private fun loadAd() {
        if(_driveScreenState.value.searchBarState.isActive || _driveScreenState.value.floatingButtonState.isBackPlateVisible)
            viewModelScope.launch(Dispatchers.IO) {
                nativeAdService.getAd()
                   .onSuccess { newAdItemGroup ->
                            _driveScreenState.update {
                                when {
                                    it.floatingButtonState.isBackPlateVisible -> {
                                        it.copy(floatingButtonState = it.floatingButtonState.copy(adItemGroup = newAdItemGroup.toItem()))
                                    }
                                    it.searchBarState.isActive -> {
                                        it.copy(searchBarState = it.searchBarState.copy(adItemGroup = newAdItemGroup.toItem()))
                                    }
                                    else -> {
                                        clearAd()
                                        it
                                    }
                                }
                            }
                    }.onFailure {
                        EventBus.send(AppEvent.SnackBar(EventMsg(R.string.network_error)))
                    }
            }
    }

    private fun clearAd() {
        viewModelScope.launch {
            _driveScreenState.update {
                it.destroyAd()
                it.copy(
                    searchBarState = it.searchBarState.copy(adItemGroup = emptyList()),
                    floatingButtonState = it.floatingButtonState.copy(adItemGroup = emptyList())
                )
            }
        }
    }

    private fun DriveScreenState.destroyAd(){
        searchBarState.adItemGroup.forEach {
            it.nativeAd.destroy()
        }
        floatingButtonState.adItemGroup.forEach {
            it.nativeAd.destroy()
        }
    }

    private suspend fun getCommentItemGroupAndUpdateCaption(checkPointId: String): List<CommentItemState> {
        val newCommentGroup = withContext(Dispatchers.IO) {
             getCommentForCheckPointUseCase(checkPointId)
        }

        if (newCommentGroup.isEmpty())
            updateCheckPointCaption(checkPointId, "")

        return newCommentGroup.map {
            val isLike = it.commentId in getHistoryStreamUseCase().first().likeGroup
            val isUserCreated = it.isMine()
            val isFocus = newCommentGroup.isFocus(it)

            if (isFocus)
                updateCheckPointCaption(checkPointId, it.toCation())

            CommentItemState(
                data = it,
                isLike = isLike,
                isFold = it.detailedReview.length >= 70,
                isFocus = isFocus,
                isUserCreated = isUserCreated,
            )
        }
    }

    private fun List<Comment>.isFocus(comment: Comment): Boolean {
        return getFocusComment().commentId == comment.commentId
    }

    private fun List<Course>.filterNearByListGroup(
        center: LatLng,
        meter: Int
    ): List<ListState.ListItemState> {
        return mapNotNull {
            val course = it.cameraLatLng
            val distance = center.distanceTo(course)
            if (distance < meter) // 근처 코스만 필터링
                ListState.ListItemState(
                    distanceFromCenter = distance,
                    course = it,
                    isBookmark = false
                )
            else
                null
        }.sortedBy { it.course.courseId }
    }

    private suspend fun Course.isMine(): Boolean {
        val profile = getUserProfileStreamUseCase().first().data
        return profile?.run { userId == profile.uid } ?: false
    }

    private suspend fun CheckPoint.isMine(): Boolean {
        val profile = getUserProfileStreamUseCase().first().data
        return profile?.run { userId == profile.uid } ?: false
    }

    private suspend fun Comment.isMine(): Boolean {
        val profile = getUserProfileStreamUseCase().first().data
        return profile?.run { userId == profile.uid } ?: false
    }

    private fun likeSwitch(commentId: String, isLike: Boolean) {
        _driveScreenState.update {
            it.run {
                val newCommentStateGroup =
                    popUpState.commentState.commentItemGroup.map {
                        if (it.data.commentId == commentId)
                            it.copy(
                                data = it.data.copy(like = it.data.like + if (isLike) -1 else 1),
                                isLike = !isLike
                            )
                        else
                            it
                    }
                copy(
                    popUpState = popUpState.copy(
                        commentState = popUpState.commentState.copy(commentItemGroup = newCommentStateGroup)
                    )
                )
            }
        }
    }

    private fun MutableStateFlow<DriveScreenState>.updateLoading(isLoading: Boolean){
        update {
            it.copy(
                bottomSheetState = it.bottomSheetState.copy(
                    checkPointAddState = it.bottomSheetState.checkPointAddState.copy(
                        isLoading = isLoading
                    )
                )
            )
        }
    }

    private fun CheckPointAddState.toRequest(courseId: String): CheckPointAddRequest {
        return CheckPointAddRequest(
            courseId = courseId,
            latLng = latlng,
            imageUri = imgUri,
            description = description
        )
    }
}
