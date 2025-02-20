package com.wheretogo.presentation.viewmodel

import android.net.Uri
import android.util.Log
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naver.maps.map.overlay.Marker
import com.wheretogo.domain.OverlayType
import com.wheretogo.domain.UseCaseFailType
import com.wheretogo.domain.model.UseCaseResponse
import com.wheretogo.domain.model.dummy.getEmogiDummy
import com.wheretogo.domain.model.map.CheckPoint
import com.wheretogo.domain.model.map.CheckPointAddRequest
import com.wheretogo.domain.model.map.Comment
import com.wheretogo.domain.model.map.Course
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.model.map.SimpleAddress
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
import com.wheretogo.domain.usecase.map.GetLatLngFromAddressUseCase
import com.wheretogo.domain.usecase.map.GetNearByCourseUseCase
import com.wheretogo.domain.usecase.map.SearchAddressUseCase
import com.wheretogo.domain.usecase.user.GetHistoryStreamUseCase
import com.wheretogo.domain.usecase.user.GetUserProfileStreamUseCase
import com.wheretogo.presentation.BuildConfig
import com.wheretogo.presentation.CameraStatus
import com.wheretogo.presentation.CheckPointAddError
import com.wheretogo.presentation.CommentType
import com.wheretogo.presentation.MarkerIconType
import com.wheretogo.presentation.feature.geo.distanceTo
import com.wheretogo.presentation.feature.naver.getMapOverlay
import com.wheretogo.presentation.feature.shortPath
import com.wheretogo.presentation.feature.withLogging
import com.wheretogo.presentation.intent.DriveScreenIntent
import com.wheretogo.presentation.model.MapOverlay
import com.wheretogo.presentation.model.OverlayTag
import com.wheretogo.presentation.parse
import com.wheretogo.presentation.state.CameraState
import com.wheretogo.presentation.state.CheckPointAddState
import com.wheretogo.presentation.state.CommentState
import com.wheretogo.presentation.state.CommentState.CommentAddState
import com.wheretogo.presentation.state.CommentState.CommentItemState
import com.wheretogo.presentation.state.DriveScreenState
import com.wheretogo.presentation.state.InfoState
import com.wheretogo.presentation.toComment
import com.wheretogo.presentation.toDomainLatLng
import com.wheretogo.presentation.toNaver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
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
    private val getLatLngFromAddressUseCase: GetLatLngFromAddressUseCase,
    private val addCheckpointToCourseUseCase: AddCheckpointToCourseUseCase,
    private val addCommentToCheckPointUseCase: AddCommentToCheckPointUseCase,
    private val removeCommentToCheckPointUseCase: RemoveCommentToCheckPointUseCase,
    private val removeCheckPointUseCase: RemoveCheckPointUseCase,
    private val removeCourseUseCase: RemoveCourseUseCase,
    private val updateLikeUseCase: UpdateLikeUseCase,
    private val reportCommentUseCase: ReportCommentUseCase,
    private val reportCourseUseCase: ReportCourseUseCase,
    private val reportCheckPointUseCase: ReportCheckPointUseCase,
    private val searchAddressUseCase: SearchAddressUseCase,
) : ViewModel() {
    private val _driveScreenState =
        MutableStateFlow(DriveScreenState()).withLogging { caller, value ->
            if(BuildConfig.DEBUG)
                caller?.let {
                    val msg =""
                    Log.d("tst_state", "${caller.shortPath()} --> $msg")
                }
        }
    private val _cacheCourseMapOverlayGroup = mutableMapOf<String, MapOverlay>() // courseId
    private val _cacheCheckPointMapOverlayGroup = mutableMapOf<String, MapOverlay>() // courseId
    val driveScreenState: StateFlow<DriveScreenState> = _driveScreenState
    private var isMapUpdate = true

    fun handleIntent(intent: DriveScreenIntent) {
        viewModelScope.launch {
            when (intent) {
                    //서치바
                    is DriveScreenIntent.AddressItemClick -> addressItemClick(intent.simpleAddress)
                    is DriveScreenIntent.SearchToggleClick -> searchToggleClick(intent.isBar)
                    is DriveScreenIntent.SubmitClick -> submitClick(intent.submit)

                    //지도
                    is DriveScreenIntent.MapIsReady -> mapIsReady()
                    is DriveScreenIntent.UpdateCamera -> updateCamara(intent.cameraState)
                    is DriveScreenIntent.OverlayRenderComplete -> overlayRenderComplete(intent.isRendered)
                    is DriveScreenIntent.CourseMarkerClick -> courseMarkerClick(intent.tag)
                    is DriveScreenIntent.CheckPointMarkerClick -> checkPointMarkerClick(intent.tag)

                    //목록
                    is DriveScreenIntent.DriveListItemClick -> driveListItemClick(intent.itemState)
                    is DriveScreenIntent.DriveListItemBookmarkClick -> driveListItemBookmarkClick(intent.itemState)

                    //팝업
                    is DriveScreenIntent.DismissPopup -> dismissPopup()
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
                    is DriveScreenIntent.FoldFloatingButtonClick -> foldFloatingButtonClick()

                    //바텀시트
                    is DriveScreenIntent.BottomSheetClose -> bottomSheetClose()
                    is DriveScreenIntent.CheckpointLocationSliderChange -> checkpointLocationSliderChange(intent.percent)
                    is DriveScreenIntent.CheckpointDescriptionChange -> checkpointDescriptionChange(intent.text)
                    is DriveScreenIntent.CheckpointDescriptionEnterClick -> checkpointDescriptionEnterClick()
                    is DriveScreenIntent.CheckpointImageChange -> checkpointImageChange(intent.imgUri)
                    is DriveScreenIntent.CheckpointSubmitClick -> checkpointSubmitClick()
                    is DriveScreenIntent.InfoReportClick -> infoReportClick(intent.infoState)
                    is DriveScreenIntent.InfoRemoveClick -> infoRemoveClick(intent.infoState)
                }
        }
    }

    //서치바
    private suspend fun addressItemClick(simpleAddress: SimpleAddress) {
        _driveScreenState.value =
            _driveScreenState.value.run { copy(searchBarState = searchBarState.copy(isLoading = true)) }
        val latlngResponse =
            withContext(Dispatchers.IO) { getLatLngFromAddressUseCase(simpleAddress.address) }

        _driveScreenState.value = _driveScreenState.value.run {
            when (latlngResponse.status) {
                UseCaseResponse.Status.Success -> {
                    val newLatLng = latlngResponse.data!!
                    copy(
                        searchBarState = searchBarState.copy(isLoading = false),
                        mapState = mapState.copy(
                            cameraState = mapState.cameraState.copy(
                                latLng = newLatLng,
                                status = CameraStatus.TRACK
                            )
                        )
                    )
                }

                UseCaseResponse.Status.Fail -> {
                    copy(searchBarState = searchBarState.copy(isLoading = false))
                }
            }
        }
    }

    private fun searchToggleClick(isBar: Boolean) {
        _driveScreenState.value.apply {
            _driveScreenState.value =
                if (!isBar) {
                    copy(
                        searchBarState = searchBarState.copy(
                            isLoading = false,
                            simpleAddressGroup = emptyList()
                        )
                    )
                } else {
                    this
                }
        }
    }

    private suspend fun submitClick(submit: String) {
        _driveScreenState.value =
            _driveScreenState.value.run { copy(searchBarState = searchBarState.copy(isLoading = true)) }
        val addressResponse = withContext(Dispatchers.IO) { searchAddressUseCase(submit) }
        _driveScreenState.value = _driveScreenState.value.run {
            when (addressResponse.status) {
                UseCaseResponse.Status.Success -> {
                    copy(
                        searchBarState = searchBarState.copy(
                            isLoading = false,
                            isEmptyVisible = addressResponse.data?.isEmpty()?:false,
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
    private fun mapIsReady() {
        _driveScreenState.apply {
            this.value = value.copy(
                mapState = _driveScreenState.value.mapState.copy(isMapReady = true)
            )
        }
    }

    private suspend fun updateCamara(cameraState: CameraState) {
        val isContentsUpdate = _driveScreenState.value.run {
            mapState.cameraState.latLng.distanceTo(cameraState.latLng) >= 1 &&
                    !bottomSheetState.isVisible &&
                    !floatingButtonState.isFoldVisible && isMapUpdate
        }


        if (isContentsUpdate) {
            isMapUpdate = false
            _driveScreenState.value = _driveScreenState.value.run { copy(isLoading = true) }
            val courseGroup =
                withContext(Dispatchers.IO) { getNearByCourseUseCase(cameraState.latLng) }
            val overlayGroup = _driveScreenState.value.mapState.mapOverlayGroup + getOverlayGroup(
                courseGroup,
                true
            )
            val listItemGroup =
                courseGroup.filterNearByListGroup(center = cameraState.latLng, meter = 3000)
            _driveScreenState.value = _driveScreenState.value.run {
                copy(
                    isLoading = false,
                    mapState = mapState.copy(
                        mapOverlayGroup = overlayGroup,
                        cameraState = cameraState
                    ),
                    listState = listState.copy(
                        listItemGroup = listItemGroup
                    )
                )
            }
            isMapUpdate = true
        }

    }

    private fun overlayRenderComplete(isRendered: Boolean) {}

    private fun courseMarkerClick(tag: OverlayTag?) {}

    private suspend fun checkPointMarkerClick(tag: OverlayTag?) {
        tag?.let { tag ->
            _driveScreenState.value = visibleInitWithLevelState(3).run {
                copy(
                    isLoading = true,
                    popUpState = popUpState.copy(
                        isVisible = true,
                        checkPointId = tag.overlayId
                    ),
                    bottomSheetState = bottomSheetState.copy(
                        infoState = bottomSheetState.infoState.copy(
                            isCourseInfo = false
                        )
                    )
                )
            }

            val checkpoint = withContext(Dispatchers.IO) {
                _driveScreenState.value.run {
                    val course = listState.clickItem.course
                    getCheckPointForMarkerUseCase(course.courseId).first { it.checkPointId == tag.overlayId }
                }
            }
            _driveScreenState.value = _driveScreenState.value.run {
                copy(
                    bottomSheetState = bottomSheetState.copy(
                        infoState = bottomSheetState.infoState.copy(
                            checkPoint = checkpoint
                        )
                    )
                )
            }
            val image = withContext(Dispatchers.IO) { getImageForPopupUseCase(checkpoint.imageName) }
            _driveScreenState.value = _driveScreenState.value.run {
                copy(
                    isLoading = false,
                    popUpState = popUpState.copy(
                        checkPointId = checkpoint.checkPointId,
                        imageUri = image
                    )
                )
            }
        }

    }


    //목록
    private suspend fun driveListItemClick(state: DriveScreenState.ListState.ListItemState) {
        val course = state.course
        _driveScreenState.value = visibleInitWithLevelState(2).run {
            mapState.mapOverlayGroup.hideCourseMapOverlayWithout(course.courseId)
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
        val checkPointGroup =
            withContext(Dispatchers.IO) { getCheckPointForMarkerUseCase(course.courseId) }
        _driveScreenState.value = _driveScreenState.value.run {
            val newMapOverlayGroup =
                mapState.mapOverlayGroup + getOverlayGroup(course.courseId, checkPointGroup, true)
            if (floatingButtonState.isFoldVisible)
                copy(
                    isLoading = false,
                    mapState = mapState.copy(
                        mapOverlayGroup = newMapOverlayGroup
                    )
                )
            else
                copy(
                    isLoading = false
                )
        }

    }

    private fun driveListItemBookmarkClick(itemState: DriveScreenState.ListState.ListItemState) {}


    //팝업
    private fun dismissPopup() {
        _driveScreenState.value = visibleInitWithLevelState(2)
    }

    private fun dismissPopupComment() {
        _driveScreenState.value = visibleInitWithLevelState(3)
    }

    private fun commentListItemClick(itemState: CommentItemState) {
        _driveScreenState.value = _driveScreenState.value.run {
            copy(
                popUpState = popUpState.copy(
                    commentState = popUpState.commentState.copy(commentItemGroup = popUpState.commentState.commentItemGroup.map {
                        if (it.data.commentId == itemState.data.commentId)
                            it.copy(isFold = !it.isFold)
                        else
                            it
                    })
                )
            )
        }
    }

    private fun commentListItemLongClick(itemState: CommentItemState) {
        val isCommentSettingVisible =
            _driveScreenState.value.popUpState.commentState.isCommentSettingVisible
        _driveScreenState.value = _driveScreenState.value.run {
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

    private suspend fun commentLikeClick(itemState: CommentItemState) {
        likeSwitch(itemState.data.commentId, itemState.isLike)
        val isLikeUpdate = withContext(Dispatchers.IO) { updateLikeUseCase(comment = itemState.data, !itemState.isLike) }
        if (!isLikeUpdate) {
            likeSwitch(itemState.data.commentId, !itemState.isLike)
        }
    }

    private suspend fun commentAddClick(itemState: CommentAddState) {
        val comment = itemState.toComment().copy(commentId = UUID.randomUUID().toString())
        val response = withContext(Dispatchers.IO) { addCommentToCheckPointUseCase(comment) }
        when (response.status) {
            UseCaseResponse.Status.Success -> {
                _driveScreenState.value = _driveScreenState.value.run {
                    this.copy(
                        popUpState = popUpState.copy(
                            commentState = popUpState.commentState.copy(commentAddState = CommentAddState(
                                groupId = comment.groupId, emogiGroup = getEmogiDummy()
                            ),
                                commentItemGroup = withContext(Dispatchers.IO) {
                                    getCommentForCheckPointUseCase(checkPointId = comment.groupId).map {
                                        CommentItemState(
                                            data = it,
                                            isLike = false,
                                            isFold = comment.detailedReview.length >= 70
                                        )
                                    }
                                })
                        )
                    )
                }
            }

            else -> {
                when (response.failType) {
                    UseCaseFailType.INVALID_USER -> {
                        Log.e("tst_vm", "msg: ${response.msg}")
                    }

                    else -> {
                        Log.e("tst_vm", "msg: ${response.msg}")
                        bottomSheetClose()
                    }
                }
            }
        }
    }

    private suspend fun commentRemoveClick(itemState: CommentItemState) {
        withContext(Dispatchers.IO) { removeCommentToCheckPointUseCase(itemState.data) }
        val checkPointId = _driveScreenState.value.popUpState.checkPointId
        val commentItemGroup = getCommentItemGroup(checkPointId)
        _driveScreenState.value = _driveScreenState.value.run {
            this.copy(
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

    private suspend fun commentReportClick(itemState: CommentItemState) {
        val reportResponse = withContext(Dispatchers.IO) { reportCommentUseCase(itemState.data) }

        when(reportResponse.status){
            UseCaseResponse.Status.Success->{
                val commentItemGroup = getCommentItemGroup(itemState.data.groupId)
                _driveScreenState.value = _driveScreenState.value.run {
                    this.copy(
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
            UseCaseResponse.Status.Fail->{

            }
        }

    }

    private fun commentEditValueChange(text: TextFieldValue) {
        _driveScreenState.value = _driveScreenState.value.run {
            this.copy(
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

    private fun commentEmogiPress(emogi: String) {
        _driveScreenState.value = _driveScreenState.value.run {
            this.copy(
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

    private fun commentTypePress(type: CommentType) {
        _driveScreenState.value = _driveScreenState.value.run {
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
            this.copy(
                popUpState = popUpState.copy(
                    commentState = popUpState.commentState.copy(
                        commentAddState = newCommentAddState
                    )
                )
            )
        }
    }


    //플로팅
    private suspend fun commentFloatingButtonClick() {
        val checkPointId = _driveScreenState.value.popUpState.checkPointId
        val emogiGroup = getEmogiDummy()
        _driveScreenState.value = _driveScreenState.value.run {
            copy(
                isLoading = true,
                popUpState = popUpState.copy(
                    commentState = popUpState.commentState.copy(
                        isCommentVisible = !popUpState.commentState.isCommentVisible,
                        commentAddState = CommentAddState(
                            groupId = checkPointId,
                            emogiGroup = emogiGroup,
                            largeEmoji = emogiGroup.firstOrNull() ?: ""
                        )
                    ),
                ),
                floatingButtonState = floatingButtonState.copy(isBackPlateVisible = false)
            )
        }
        val commentItemState = getCommentItemGroup(checkPointId)
        _driveScreenState.value = _driveScreenState.value.run {
            if (popUpState.commentState.isCommentVisible)
                copy(
                    isLoading = false,
                    popUpState = popUpState.copy(
                        commentState = popUpState.commentState.copy(
                            commentItemGroup = commentItemState
                        )
                    )
                )
            else
                this
        }
    }

    private suspend fun checkpointAddFloatingButtonClick() {
        _driveScreenState.value = _driveScreenState.value.run {
            val newCheckPointAddState = bottomSheetState.checkPointAddState.run {
                val course = listState.clickItem.course
                val newMarker = listOf(Marker().apply {
                    val latlng = course.waypoints.first()
                    zIndex = 999
                    tag = "${latlng.latitude}${latlng.longitude}"
                    position = latlng.toNaver()
                })
                val newMapOverlay = MapOverlay(
                    overlayId = "new",
                    overlayType = OverlayType.CHECKPOINT,
                    iconType = MarkerIconType.PHOTO,
                    markerGroup = newMarker
                )
                copy(
                    sliderPercent = 0.0f,
                    addMarker = newMapOverlay,
                )
            }
            val newMapState =
                mapState.copy(mapOverlayGroup = mapState.mapOverlayGroup + newCheckPointAddState.addMarker)
            copy(
                mapState = newMapState,
                bottomSheetState = bottomSheetState.copy(
                    isVisible = !bottomSheetState.isVisible,
                    isCheckPointAdd = true,
                    checkPointAddState = newCheckPointAddState
                )
            )
        }
    }

    private suspend fun infoFloatingButtonClick() {
        _driveScreenState.value = _driveScreenState.value.run {
            val isMine = if(bottomSheetState.infoState.isCourseInfo){
                bottomSheetState.infoState.course.isMine()
            }else{
                bottomSheetState.infoState.checkPoint.isMine()
            }
            copy(
                bottomSheetState = bottomSheetState.copy(
                    isVisible = !bottomSheetState.isVisible,
                    isCheckPointAdd = false,
                    infoState = bottomSheetState.infoState.copy(
                        course = listState.clickItem.course,
                        isRemoveButton = isMine,
                        isReportButton = true
                    )
                )
            )
        }
    }

    private suspend fun exportMapFloatingButtonClick() {
        _driveScreenState.value = _driveScreenState.value.run {
            copy(
                floatingButtonState = floatingButtonState.copy(
                    isBackPlateVisible = !floatingButtonState.isBackPlateVisible
                )
            )
        }
    }

    private suspend fun foldFloatingButtonClick() {
        val course = _driveScreenState.value.listState.clickItem.course
        _driveScreenState.value = visibleInitWithLevelState(1).apply {
            mapState.mapOverlayGroup.showCourseMapOverlay()
        }

        val checkpointGroup = _driveScreenState.value.run {
            withContext(Dispatchers.IO) { getCheckPointForMarkerUseCase(course.courseId) }
        }

        _driveScreenState.value = _driveScreenState.value.run {
            val newMapData =
                mapState.mapOverlayGroup - getOverlayGroup(
                    listState.clickItem.course.courseId,
                    checkpointGroup,
                    false
                ).toSet()

            copy(
                mapState = mapState.copy(
                    mapOverlayGroup = newMapData
                )
            )
        }
    }


    //바텀시트
    private fun bottomSheetClose() {
        _driveScreenState.value = _driveScreenState.value.run {
            bottomSheetState.checkPointAddState.addMarker.markerGroup.firstOrNull()
                ?.apply { map = null }
            val newMapOverlayGroup =
                mapState.mapOverlayGroup.filter { it.overlayId != "new" }.toSet()
            this.copy(
                mapState = mapState.copy(mapOverlayGroup = newMapOverlayGroup),
                bottomSheetState = bottomSheetState.copy(
                    isVisible = false,
                    checkPointAddState = CheckPointAddState()
                ),
            )
        }
    }

    private suspend fun checkpointLocationSliderChange(percent: Float) {
        _driveScreenState.value = _driveScreenState.value.run {
            val newCheckPointAddState = bottomSheetState.checkPointAddState.run {
                val points = listState.clickItem.course.points
                val newMarker = addMarker.markerGroup.first().apply {
                    val newLatlng = points[round(points.size * percent).toInt()]
                    position = newLatlng.toNaver()
                    tag = "${newLatlng.latitude}${newLatlng.latitude}"
                }
                val newMapOverlay = addMarker.copy(
                    markerGroup = listOf(newMarker)
                )
                copy(
                    sliderPercent = percent,
                    addMarker = newMapOverlay
                )
            }.run { copy(isSubmitActive = isValidateAddCheckPoint().isSuccess) }
            copy(bottomSheetState = bottomSheetState.copy(checkPointAddState = newCheckPointAddState))
        }
    }

    private suspend fun checkpointDescriptionChange(text: String) {
        _driveScreenState.value = _driveScreenState.value.run {
            val newCheckPointAddState = bottomSheetState.checkPointAddState.run {
                copy(
                    description = text
                )
            }.run { copy(isSubmitActive = isValidateAddCheckPoint().isSuccess) }
            copy(bottomSheetState = bottomSheetState.copy(checkPointAddState = newCheckPointAddState))
        }
    }

    private suspend fun checkpointImageChange(imgUri: Uri?) {
        imgUri?.let {
            _driveScreenState.value = _driveScreenState.value.run {
                val newCheckPointAddState = bottomSheetState.checkPointAddState.run {
                    copy(
                        imgUri = imgUri,
                        imgInfo = withContext(Dispatchers.IO) { getImageInfoUseCase(imgUri) }
                    )
                }.run { copy(isSubmitActive = isValidateAddCheckPoint().isSuccess) }
                copy(bottomSheetState = bottomSheetState.copy(checkPointAddState = newCheckPointAddState))
            }
        }
    }

    private suspend fun checkpointSubmitClick() {
        val course = _driveScreenState.value.listState.clickItem.course
        if (_driveScreenState.value.bottomSheetState.checkPointAddState.isSubmitActive) {
            _driveScreenState.value = _driveScreenState.value.run {
                copy(
                    bottomSheetState = bottomSheetState.copy(
                        checkPointAddState = bottomSheetState.checkPointAddState.copy(
                            isLoading = true
                        )
                    )
                )
            }


            val newCheckpointAddRequest = _driveScreenState.value.run {
                CheckPointAddRequest(
                    courseId = course.courseId,
                    latLng = bottomSheetState.checkPointAddState.addMarker.markerGroup.first().position.toDomainLatLng(),
                    imageName = bottomSheetState.checkPointAddState.imgInfo?.fileName ?: "",
                    imageUri = bottomSheetState.checkPointAddState.imgUri,
                    description = bottomSheetState.checkPointAddState.description
                )
            }

            val addCheckPointResponse = withContext(Dispatchers.IO) { addCheckpointToCourseUseCase(newCheckpointAddRequest) }
            when (addCheckPointResponse.status) {
                UseCaseResponse.Status.Success -> {
                    val checkPointGroup = withContext(Dispatchers.IO) { getCheckPointForMarkerUseCase(course.courseId) }
                    _driveScreenState.value = _driveScreenState.value.run {
                        val newMapOverlayGroup =
                            mapState.mapOverlayGroup + getOverlayGroup(
                                course.courseId,
                                checkPointGroup,
                                true
                            )
                        visibleInitWithLevelState(2).copy(
                            mapState = mapState.copy(
                                mapOverlayGroup = newMapOverlayGroup
                            ),
                            listState = listState.copy(
                                clickItem = listState.clickItem.copy(
                                    course = course.copy(
                                        checkpointIdGroup = checkPointGroup.map { it.checkPointId }
                                    )
                                )
                            ),
                            bottomSheetState = bottomSheetState.copy(
                                checkPointAddState = bottomSheetState.checkPointAddState.copy(
                                    isLoading = false
                                )
                            )
                        )
                    }
                    bottomSheetClose()
                }

                else -> {}
            }
        }
    }

    private fun checkpointDescriptionEnterClick() {}

    private suspend fun infoRemoveClick(infoState: InfoState) {
        _driveScreenState.value = _driveScreenState.value.run { copy(isLoading = true) }
        if (infoState.isCourseInfo) { // 코스
            val reportResponse = withContext(Dispatchers.IO) { removeCourseUseCase(infoState.course.courseId) }
            when(reportResponse.status){
                UseCaseResponse.Status.Success->{
                    removeCourseMapOverlay(infoState.course.courseId)
                    infoState.course.checkpointIdGroup.forEach { removeCheckPointMapOverlay(it) }
                    closeAndInitCommentBottomSheet()
                }
                else->{
                    _driveScreenState.value = _driveScreenState.value.copy(isLoading = false)
                }
            }

        } else { // 체크포인트
            val reportResponse = withContext(Dispatchers.IO) {
                removeCheckPointUseCase(infoState.course.courseId, infoState.checkPoint.checkPointId)
            }
            when(reportResponse.status){
                UseCaseResponse.Status.Success->{
                    removeCheckPointMapOverlay(infoState.checkPoint.checkPointId)
                    closeAndInitCommentBottomSheet()
                }
                else->{
                    _driveScreenState.value = _driveScreenState.value.copy(isLoading = false)
                }
            }
        }
    }

    private suspend fun infoReportClick(infoState: InfoState) {
        _driveScreenState.value = _driveScreenState.value.copy(isLoading = true)
        if (infoState.isCourseInfo) { // 코스
            val reportResponse = withContext(Dispatchers.IO) {
                reportCourseUseCase(infoState.course, infoState.reason) }
            when(reportResponse.status){
                UseCaseResponse.Status.Success->{
                    removeCourseMapOverlay(infoState.course.courseId)
                    infoState.course.checkpointIdGroup.forEach {
                        removeCheckPointMapOverlay(it)
                    }
                    closeAndInitCommentBottomSheet()
                }
                else->{
                    _driveScreenState.value = _driveScreenState.value.copy(isLoading = false)
                }
            }

        } else { // 체크포인트
            val reportResponse = withContext(Dispatchers.IO) {
                reportCheckPointUseCase(infoState.checkPoint.checkPointId, "")
            }

            when(reportResponse.status){
                UseCaseResponse.Status.Success->{
                    removeCheckPointMapOverlay(infoState.checkPoint.checkPointId)
                    closeAndInitCommentBottomSheet()
                }
                else->{
                    _driveScreenState.value = _driveScreenState.value.copy(isLoading = false)
                }
            }
        }


    }

    private suspend fun closeAndInitCommentBottomSheet(){
        val cameraLatLng = _driveScreenState.value.mapState.cameraState.latLng
        val newCourseGroup = withContext(Dispatchers.IO) { getNearByCourseUseCase(cameraLatLng) }
        _driveScreenState.value = _driveScreenState.value.run {
            val newMapOverlay = getOverlayGroup(newCourseGroup,false)
            val newList = newCourseGroup.filterNearByListGroup(
                center = cameraLatLng,
                meter = 3000
            )
            run {
                if (bottomSheetState.infoState.isCourseInfo) {
                    visibleInitWithLevelState(1)
                } else {
                    visibleInitWithLevelState(2)
                }
            }.run {
                copy(
                    isLoading = false,
                    mapState = mapState.copy(
                        mapOverlayGroup = newMapOverlay
                    ),
                    listState = listState.copy(
                        listItemGroup = newList
                    )
                )
            }
        }
    }



    private fun CheckPointAddState.isValidateAddCheckPoint(): Result<Unit> {
        return runCatching {
            require(this.imgUri != null) { CheckPointAddError.EMPTY_IMG }
            require(this.description.isNotEmpty()) { CheckPointAddError.EMPTY_DESCRIPTION }
        }
    }

    private fun visibleInitWithLevelState(level: Int): DriveScreenState {
        Log.d("tst_", "visibleInitWithLevel: $level")
        return when (level) {
            1 -> {//목록
                _driveScreenState.value.run {
                    copy(
                            searchBarState = searchBarState.copy(isVisible = true),
                            listState = listState.copy(isVisible = true,
                                clickItem = DriveScreenState.ListState.ListItemState()),
                            popUpState = popUpState.copy(isVisible = false),
                            floatingButtonState = DriveScreenState.FloatingButtonState(),
                            bottomSheetState = DriveScreenState.BottomSheetState()
                    )
                }
            }

            2 -> {//코스
                _driveScreenState.value.run {
                    copy(
                        searchBarState = searchBarState.copy(isVisible = false),
                        listState = listState.copy(isVisible = false),
                        popUpState = DriveScreenState.PopUpState(isVisible = false),
                        floatingButtonState = DriveScreenState.FloatingButtonState(
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
                _driveScreenState.value.run {
                    copy(
                        searchBarState = searchBarState.copy(isVisible = false),
                        floatingButtonState = DriveScreenState.FloatingButtonState(
                            true, false, true, true, false, true
                        ),
                        popUpState = popUpState.copy(
                            commentState = CommentState()
                        )
                    )
                }

            }

            4 -> {//바텀시트
                _driveScreenState.value.run {
                    copy(
                        searchBarState = searchBarState.copy(isVisible = false),
                        listState = listState.copy(isVisible = false),
                        popUpState = popUpState.copy(isVisible = false),
                        floatingButtonState = DriveScreenState.FloatingButtonState(),
                        bottomSheetState = DriveScreenState.BottomSheetState()
                    )
                }
            }

            else -> _driveScreenState.value
        }
    }


    //유틸
    private suspend fun getCommentItemGroup(checkPointId: String): List<CommentItemState> {
        return withContext(Dispatchers.IO) {
            getCommentForCheckPointUseCase(checkPointId).map {
                val isLike = it.commentId in getHistoryStreamUseCase().first().likeGroup
                val isUserCreated = it.isMine()
                CommentItemState(
                    data = it,
                    isLike = isLike,
                    isFold = it.detailedReview.length >= 70,
                    isUserCreated = isUserCreated
                )
            }
        }
    }

    private fun List<Course>.filterNearByListGroup(
        center: LatLng,
        meter: Int
    ): List<DriveScreenState.ListState.ListItemState> {
        return mapNotNull {
            val course = it.cameraLatLng
            val distance = center.distanceTo(course)
            if (distance < meter) // 근처 코스만 필터링
                DriveScreenState.ListState.ListItemState(
                    distanceFromCenter = distance,
                    course = it,
                    isBookmark = false
                )
            else
                null
        }.sortedBy { it.course.courseId }
    }

    private suspend fun getOverlayGroup(
        courseId: String,
        checkPoints: List<CheckPoint>,
        visible: Boolean
    ): Set<MapOverlay> {
        return coroutineScope {
            checkPoints.map { checkPoint ->
                async {
                    _cacheCheckPointMapOverlayGroup.getOrPut(checkPoint.checkPointId) {
                        getMapOverlay(courseId, checkPoint)
                    }.apply {
                        this.markerGroup.forEach { it.isVisible = visible }
                    }
                }
            }.awaitAll()
        }.toSet()
    }

    private fun getOverlayGroup(
        courseGroup: List<Course>,
        visible: Boolean = false
    ): Set<MapOverlay> {
        return courseGroup.map {
            _cacheCourseMapOverlayGroup.getOrPut(it.courseId) { getMapOverlay(it) }
        }.toSet().apply { if (visible) showCourseMapOverlay() }
    }

    private fun Set<MapOverlay>.hideCourseMapOverlayWithout(withoutOverlyId: String) {
        onEach {
            if (OverlayType.COURSE == it.overlayType)
                if (it.overlayId != withoutOverlyId) {
                    it.markerGroup.forEach { it.isVisible = false }
                    it.path?.isVisible = false
                }
        }
    }

    private fun Set<MapOverlay>.showCourseMapOverlay() {
        onEach {
            it.markerGroup.forEach { it.isVisible = true }
            it.path?.isVisible = true
        }
    }

    private suspend fun List<CheckPoint>.imagePreLoad() {
        coroutineScope {
            forEach {
                launch(Dispatchers.IO) {
                    getImageForPopupUseCase(it.imageName)  // 체크포인트 이미지 미리로드
                }
            }
        }
    }

    private suspend fun List<CheckPoint>.commentPreLoad() {
        coroutineScope {
            forEach {
                launch(Dispatchers.IO) {
                    getCommentForCheckPointUseCase(it.checkPointId)
                }
            }
        }
    }

    private fun removeCourseMapOverlay(courseId: String) {
        _cacheCourseMapOverlayGroup.get(courseId)?.apply {
            path?.map = null
            markerGroup.forEach {
                it.map = null
            }
        }
        _cacheCourseMapOverlayGroup.remove(courseId)
    }

    private fun removeCheckPointMapOverlay(checkPointId: String) {
        val newOverlay = _cacheCheckPointMapOverlayGroup.get(checkPointId)?.run {
            val newMarkerGroup = this.markerGroup.mapNotNull {
                val cid = OverlayTag.parse(it.tag as String)!!.overlayId
                if (checkPointId == cid) {
                    it.map = null
                    null
                } else {
                    it
                }
            }
            this.copy(
                markerGroup = newMarkerGroup
            )
        }
        newOverlay?.let {
            _cacheCheckPointMapOverlayGroup.set(checkPointId, it)
        }
    }


    private suspend fun Course.isMine():Boolean{
        val profile =  getUserProfileStreamUseCase().first().data
        return profile?.run { userId == profile.uid  }?:false
    }
    private suspend fun CheckPoint.isMine():Boolean{
        val profile =  getUserProfileStreamUseCase().first().data
        return profile?.run { userId == profile.uid  }?:false
    }
    private suspend fun Comment.isMine():Boolean{
        val profile =  getUserProfileStreamUseCase().first().data
        return profile?.run { userId == profile.uid  }?:false
    }

    private fun likeSwitch(commentId:String, isLike:Boolean){
        val isVisible = _driveScreenState.value.popUpState.commentState.isCommentVisible
        if(isVisible){
            _driveScreenState.value = _driveScreenState.value.run {
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
}
