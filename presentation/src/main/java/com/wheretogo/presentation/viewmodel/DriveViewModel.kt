package com.wheretogo.presentation.viewmodel

import android.net.Uri
import android.util.Log
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naver.maps.map.overlay.Marker
import com.wheretogo.domain.HistoryType
import com.wheretogo.domain.OverlayType
import com.wheretogo.domain.UseCaseFailType
import com.wheretogo.domain.model.UseCaseResponse
import com.wheretogo.domain.model.dummy.getEmogiDummy
import com.wheretogo.domain.model.map.Address
import com.wheretogo.domain.model.map.SimpleAddress
import com.wheretogo.domain.model.map.CheckPoint
import com.wheretogo.domain.model.map.CheckPointAddRequest
import com.wheretogo.domain.model.map.Course
import com.wheretogo.domain.model.map.History
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.model.user.Profile
import com.wheretogo.domain.usecase.community.AddCommentToCheckPointUseCase
import com.wheretogo.domain.usecase.community.GetCommentForCheckPointUseCase
import com.wheretogo.domain.usecase.community.GetImageInfoUseCase
import com.wheretogo.domain.usecase.community.ModifyLikeUseCase
import com.wheretogo.domain.usecase.community.RemoveCheckPointUseCase
import com.wheretogo.domain.usecase.community.RemoveCommentToCheckPointUseCase
import com.wheretogo.domain.usecase.community.RemoveCourseUseCase
import com.wheretogo.domain.usecase.community.ReportCheckPointUseCase
import com.wheretogo.domain.usecase.community.ReportCommentUseCase
import com.wheretogo.domain.usecase.community.ReportCourseUseCase
import com.wheretogo.domain.usecase.map.AddCheckpointToCourseUseCase
//import com.wheretogo.domain.usecase.map.GetAddressBySearchUseCase
import com.wheretogo.domain.usecase.map.GetCheckpointForMarkerUseCase
import com.wheretogo.domain.usecase.map.GetImageForPopupUseCase
import com.wheretogo.domain.usecase.map.GetNearByCourseUseCase
import com.wheretogo.domain.usecase.user.GetHistoryStreamUseCase
import com.wheretogo.domain.usecase.user.GetUserProfileStreamUseCase
import com.wheretogo.domain.usecase.user.RemoveHistoryUseCase
import com.wheretogo.domain.usecase.user.UpdateHistoryUseCase
import com.wheretogo.presentation.CameraStatus
import com.wheretogo.presentation.CheckPointAddError
import com.wheretogo.presentation.CommentType
import com.wheretogo.presentation.MarkerIconType
import com.wheretogo.presentation.feature.geo.distanceTo
import com.wheretogo.presentation.feature.naver.getMapOverlay
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
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
    //private val getAddressBySearchUseCase: GetAddressBySearchUseCase,
    private val addCheckpointToCourseUseCase: AddCheckpointToCourseUseCase,
    private val addCommentToCheckPointUseCase: AddCommentToCheckPointUseCase,
    private val removeCommentToCheckPointUseCase: RemoveCommentToCheckPointUseCase,
    private val removeCheckPointUseCase: RemoveCheckPointUseCase,
    private val removeCourseUseCase: RemoveCourseUseCase,
    private val removeHistoryUseCase: RemoveHistoryUseCase,
    private val updateHistoryUseCase: UpdateHistoryUseCase,
    private val modifyLikeUseCase: ModifyLikeUseCase,
    private val reportCommentUseCase: ReportCommentUseCase,
    private val reportCourseUseCase: ReportCourseUseCase,
    private val reportCheckPointUseCase: ReportCheckPointUseCase,
) : ViewModel() {
    private val _driveScreenState = MutableStateFlow(DriveScreenState())
    private val _cacheCourseMapOverlayGroup = mutableMapOf<String, MapOverlay>() // courseId
    private val _cacheCheckPointMapOverlayGroup = mutableMapOf<String, MapOverlay>() // courseId
    val driveScreenState: StateFlow<DriveScreenState> = _driveScreenState

    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        when (exception) {
            else -> {
                _driveScreenState.value = DriveScreenState(
                    isLoading = false,
                    error = exception.message
                )
                exception.printStackTrace()
            }
        }
    }

    fun handleIntent(intent: DriveScreenIntent) {
        viewModelScope.launch(exceptionHandler) {
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

    init {
        viewModelScope.launch(exceptionHandler) {
            _driveScreenState.combine(getHistoryStreamUseCase(), getUserProfileStreamUseCase())
        }
    }


    //서치바
    private fun addressItemClick(simpleAddress: SimpleAddress){
        _driveScreenState.value.apply {
            _driveScreenState.value = run { copy(searchBarState = searchBarState.copy(isLoading = true)) }
            _driveScreenState.value = run {
                val newLatLng = LatLng(latitude = 37.3595704, 127.105399) // todo 실제 데이터 연결
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
        }
    }

    private fun searchToggleClick(isBar:Boolean){
        _driveScreenState.value.apply {
            _driveScreenState.value = run {
                if(!isBar){
                    copy(
                        searchBarState = searchBarState.copy(
                            isLoading = false,
                            simpleAddressGroup = emptyList()
                        )
                    )
                }else{
                    copy()
                }
            }
        }
    }

    private suspend fun submitClick(submit:String){
        _driveScreenState.value.apply {
            _driveScreenState.value = run { copy(searchBarState = searchBarState.copy(isLoading = true)) }
            _driveScreenState.value = run {
                val newAddressGroup = listOf(SimpleAddress(submit, "${submit}Address")) // todo 실제 데이터 연결
                copy(
                    searchBarState = searchBarState.copy(
                        isLoading = false,
                        simpleAddressGroup = newAddressGroup
                    )
                )
            }


         /*   _driveScreenState.value = run {
                val addressResponse = getAddressBySearchUseCase(submit)
                when(addressResponse.status){
                    UseCaseResponse.Status.Success->{
                        copy(
                            searchBarState = searchBarState.copy(
                                isLoading = false,
                                simpleAddressGroup = addressResponse.data?: emptyList()
                            )
                        )
                    }
                    UseCaseResponse.Status.Fail->{
                        copy(
                            searchBarState = searchBarState.copy(
                                isLoading = false
                            )
                        )
                    }
                }

            }*/
        }
    }


    //지도
    private fun mapIsReady() {
        _driveScreenState.value = _driveScreenState.value.copy(
            mapState = _driveScreenState.value.mapState.copy(isMapReady = true)
        )
    }

    private suspend fun updateCamara(cameraState: CameraState) {
        _driveScreenState.value.apply {
            if (mapState.cameraState.latLng.distanceTo(cameraState.latLng) >= 1 &&
                !bottomSheetState.isVisible &&
                !floatingButtonState.isFoldVisible
            ) {
                val courseGroup = getNearByCourseUseCase(cameraState.latLng)
                val overlayGroup = mapState.mapOverlayGroup + getOverlayGroup(courseGroup, true)
                val listItemGroup =
                    getNearByListDataGroup(center = cameraState.latLng, meter = 3000, courseGroup)
                _driveScreenState.value = copy(
                    mapState = mapState.copy(
                        mapOverlayGroup = overlayGroup, cameraState = cameraState
                    ), listState = _driveScreenState.value.listState.copy(
                        listItemGroup = listItemGroup
                    )
                )
            }
        }
    }

    private fun overlayRenderComplete(isRendered: Boolean) {
        _driveScreenState.value = _driveScreenState.value.copy(isLoading = !isRendered)
    }

    private suspend fun courseMarkerClick(tag: OverlayTag?) {

    }

    private suspend fun checkPointMarkerClick(tag: OverlayTag?) {
        tag?.let { tag ->
            _driveScreenState.value = _driveScreenState.value.run {
                val course = listState.clickItem.course
                val checkpoint =
                    getCheckPointForMarkerUseCase(course.courseId).first { it.checkPointId == tag.overlayId }
                val image = getImageForPopupUseCase(checkpoint.imageName)
                copy(
                    popUpState = popUpState.copy(
                        checkPointId = checkpoint.checkPointId,
                        imageUri = image,
                    ),
                    bottomSheetState = bottomSheetState.copy(
                        infoState = bottomSheetState.infoState.copy(
                            isCourseInfo = false,
                            checkPoint = checkpoint
                        )
                    ),

                    )
            }
            visibleInitWithLevel(3)
        }

    }


    //목록
    private suspend fun driveListItemClick(state: DriveScreenState.ListState.ListItemState) {
        val course = state.course
        val checkPointGroup = getCheckPointForMarkerUseCase(course.courseId)

        _driveScreenState.value = _driveScreenState.value.run {
            mapState.mapOverlayGroup.hideCourseMapOverlayWithout(course.courseId)
            copy(isLoading = true)
        }

        _driveScreenState.value = _driveScreenState.value.run {
            val newMapOverlayGroup =
                mapState.mapOverlayGroup + getOverlayGroup(course.courseId, checkPointGroup, true)
            copy(
                mapState = mapState.copy(
                    mapOverlayGroup = newMapOverlayGroup
                ),
                listState = listState.copy(
                    clickItem = state
                ),
                bottomSheetState = bottomSheetState.copy(
                    infoState = bottomSheetState.infoState.copy(
                        isCourseInfo = true,
                        course = course
                    )
                )
            )
        }
        visibleInitWithLevel(2)
        checkPointGroup.imagePreLoad()
        checkPointGroup.commentPreLoad()
        _driveScreenState.value = _driveScreenState.value.copy(isLoading = false)
    }

    private suspend fun driveListItemBookmarkClick(itemState: DriveScreenState.ListState.ListItemState) {
        if (itemState.isBookmark)
            removeHistoryUseCase(itemState.course.courseId, HistoryType.BOOKMARK)
        else
            updateHistoryUseCase(itemState.course.courseId, HistoryType.BOOKMARK)

    }


    //팝업
    private fun dismissPopup() {
        visibleInitWithLevel(2)
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
        if (modifyLikeUseCase(comment = itemState.data, !itemState.isLike)) {
            val newCommentStateGroup =
                _driveScreenState.value.popUpState.commentState.commentItemGroup.map {
                    if (it.data.commentId == itemState.data.commentId)
                        it.copy(data = it.data.copy(like = it.data.like + if (itemState.isLike) -1 else 1))
                    else
                        it
                }

            _driveScreenState.value = _driveScreenState.value.run {
                copy(
                    popUpState = popUpState.copy(
                        commentState = popUpState.commentState.copy(commentItemGroup = newCommentStateGroup)
                    )
                )
            }

        }
    }

    private suspend fun commentAddClick(itemState: CommentAddState) {
        val comment = itemState.toComment().copy(commentId = UUID.randomUUID().toString())
        val response = addCommentToCheckPointUseCase(comment)
        when (response.status) {
            UseCaseResponse.Status.Success -> {
                _driveScreenState.value = _driveScreenState.value.run {
                    this.copy(
                        popUpState = popUpState.copy(
                            commentState = popUpState.commentState.copy(
                                commentAddState = CommentAddState(
                                    groupId = comment.groupId,
                                    emogiGroup = getEmogiDummy()
                                ),
                                commentItemGroup = getCommentForCheckPointUseCase(checkPointId = comment.groupId).map {
                                    CommentItemState(
                                        data = it,
                                        isLike = false,
                                        isFold = comment.detailedReview.length >= 70
                                    )
                                }
                            )
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
        removeCommentToCheckPointUseCase(itemState.data)
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
        reportCommentUseCase(itemState.data)
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
        val commentItemState = getCommentItemGroup(checkPointId)
        val emogiGroup = getEmogiDummy()
        _driveScreenState.value = _driveScreenState.value.run {
            copy(
                popUpState = popUpState.copy(
                    commentState = popUpState.commentState.copy(
                        isCommentVisible = !popUpState.commentState.isCommentVisible,
                        commentItemGroup = commentItemState,
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
    }

    private fun checkpointAddFloatingButtonClick() {
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

    private fun infoFloatingButtonClick() {
        _driveScreenState.value = _driveScreenState.value.run {
            copy(
                bottomSheetState = bottomSheetState.copy(
                    isVisible = !bottomSheetState.isVisible,
                    isCheckPointAdd = false,
                    infoState = bottomSheetState.infoState.copy(
                        course = listState.clickItem.course
                    )
                )
            )
        }
    }

    private fun exportMapFloatingButtonClick() {
        _driveScreenState.value = _driveScreenState.value.run {
            copy(
                floatingButtonState = floatingButtonState.copy(
                    isBackPlateVisible = !floatingButtonState.isBackPlateVisible
                )
            )
        }
    }

    private suspend fun foldFloatingButtonClick() {
        coroutineScope {
            _driveScreenState.value = _driveScreenState.value.run {
                val course = listState.clickItem.course
                val checkpointGroup = getCheckPointForMarkerUseCase(course.courseId)
                val newMapData =
                    mapState.mapOverlayGroup - getOverlayGroup(
                        course.courseId,
                        checkpointGroup,
                        false
                    ).toSet()
                copy(
                    mapState = mapState.copy(
                        mapOverlayGroup = newMapData
                    )
                )
            }
            visibleInitWithLevel(1)
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

    private fun checkpointLocationSliderChange(percent: Float) {
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

    private fun checkpointDescriptionChange(text: String) {
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
                        imgInfo = getImageInfoUseCase(imgUri)
                    )
                }.run { copy(isSubmitActive = isValidateAddCheckPoint().isSuccess) }
                copy(bottomSheetState = bottomSheetState.copy(checkPointAddState = newCheckPointAddState))
            }
        }
    }

    private suspend fun checkpointSubmitClick() {
        _driveScreenState.value.apply {
            if (bottomSheetState.checkPointAddState.isSubmitActive) {
                _driveScreenState.value =
                    copy(
                        bottomSheetState = bottomSheetState.copy(
                            checkPointAddState = bottomSheetState.checkPointAddState.copy(
                                isLoading = true
                            )
                        )
                    )
                val courseId = listState.clickItem.course.courseId
                val newCheckpointAddRequest = CheckPointAddRequest(
                    courseId = courseId,
                    latLng = bottomSheetState.checkPointAddState.addMarker.markerGroup.first().position.toDomainLatLng(),
                    imageName = bottomSheetState.checkPointAddState.imgInfo?.fileName ?: "",
                    imageUri = bottomSheetState.checkPointAddState.imgUri,
                    description = bottomSheetState.checkPointAddState.description
                )

                when (addCheckpointToCourseUseCase(newCheckpointAddRequest).status) {
                    UseCaseResponse.Status.Success -> {
                        bottomSheetClose()
                        val course = listState.clickItem.course
                        val checkPointGroup = getCheckPointForMarkerUseCase(courseId)
                        _driveScreenState.value = _driveScreenState.value.run {
                            val newMapOverlayGroup =
                                mapState.mapOverlayGroup + getOverlayGroup(
                                    course.courseId,
                                    checkPointGroup,
                                    true
                                )
                            copy(
                                mapState = mapState.copy(
                                    mapOverlayGroup = newMapOverlayGroup
                                ),
                                listState = listState.copy(
                                    clickItem = listState.clickItem.copy(
                                        course = course.copy(
                                            checkpointIdGroup = checkPointGroup.map { it.checkPointId }
                                        )
                                    )
                                )
                            )
                        }

                    }

                    else -> {}
                }
                visibleInitWithLevel(2)
            }
        }
    }

    private fun checkpointDescriptionEnterClick() {
        _driveScreenState.value = _driveScreenState.value.run {
            copy()
        }
    }

    private suspend fun infoRemoveClick(infoState: InfoState) {
        _driveScreenState.value = _driveScreenState.value.run { copy(isLoading = true) }
        if (infoState.isCourseInfo) {
            removeCourseUseCase(infoState.course.courseId)
            removeCourseMapOverlay(infoState.course.courseId)
            infoState.course.checkpointIdGroup.forEach {
                removeCheckPointMapOverlay(it)
            }
        } else {
            removeCheckPointUseCase(infoState.course.courseId, infoState.checkPoint.checkPointId)
            removeCheckPointMapOverlay(infoState.checkPoint.checkPointId)
        }

        _driveScreenState.value = _driveScreenState.value.run {
            val newCourseGroup = getNearByCourseUseCase(mapState.cameraState.latLng)
            val newMapOverlay = getOverlayGroup(newCourseGroup)
            val newList = getNearByListDataGroup(
                center = mapState.cameraState.latLng,
                meter = 3000,
                newCourseGroup
            )
            copy(
                isLoading = false,
                mapState = mapState.copy(
                    mapOverlayGroup = newMapOverlay
                ),
                listState = DriveScreenState.ListState(
                    isVisible = true,
                    listItemGroup = newList,
                    clickItem = DriveScreenState.ListState.ListItemState()
                )
            )
        }
        _driveScreenState.value.run {
            if (bottomSheetState.infoState.isCourseInfo)
                visibleInitWithLevel(1)
            else
                visibleInitWithLevel(2)
        }
    }

    private suspend fun infoReportClick(infoState: InfoState) {
        _driveScreenState.value = _driveScreenState.value.run { copy(isLoading = true) }

        if (infoState.isCourseInfo) {
            reportCourseUseCase(infoState.course, infoState.reason)
            removeCourseMapOverlay(infoState.course.courseId)
            infoState.course.checkpointIdGroup.forEach {
                removeCheckPointMapOverlay(it)
            }
        } else {
            reportCheckPointUseCase(infoState.checkPoint.checkPointId, "")
            removeCheckPointMapOverlay(infoState.checkPoint.checkPointId)
        }

        _driveScreenState.value = _driveScreenState.value.run {
            val newCourseGroup = getNearByCourseUseCase(mapState.cameraState.latLng)
            val newMapOverlay = getOverlayGroup(newCourseGroup)
            val newList = getNearByListDataGroup(
                center = mapState.cameraState.latLng,
                meter = 3000,
                newCourseGroup
            )
            copy(
                isLoading = false,
                mapState = mapState.copy(
                    mapOverlayGroup = newMapOverlay
                ),
                listState = listState.copy(
                    isVisible = true,
                    listItemGroup = newList,
                    clickItem = DriveScreenState.ListState.ListItemState()
                )
            )
        }

        _driveScreenState.value.run {
            if (bottomSheetState.infoState.isCourseInfo)
                visibleInitWithLevel(1)
            else
                visibleInitWithLevel(2)
        }
    }

    private fun CheckPointAddState.isValidateAddCheckPoint(): Result<Unit> {
        return runCatching {
            require(this.imgUri != null) { CheckPointAddError.EMPTY_IMG }
            require(this.description.isNotEmpty()) { CheckPointAddError.EMPTY_DESCRIPTION }
        }
    }

    // UI 공통
    private suspend fun MutableStateFlow<DriveScreenState>.combine(
        historyFlow: Flow<History>,
        profileFlow: Flow<UseCaseResponse<Profile>>
    ) {
        combine(this, historyFlow, profileFlow) { state, history, profile ->
            state.run {
                val isAdmin = profile.data?.private?.isAdmin?:false
                val listItemGroup = listState.copy().listItemGroup.map {
                    it.copy(isBookmark = it.course.courseId in history.bookmarkGroup)
                }
                val commentGroup = popUpState.commentState.commentItemGroup.map {
                    val isLike = it.data.commentId in history.likeGroup
                    val isUserCreated = it.data.commentId in history.commentGroup

                    it.copy(
                        data = it.data,
                        isLike = isLike,
                        isUserCreated = isUserCreated
                    )
                }
                val isInfoRemove = bottomSheetState.infoState.run {
                    if (isCourseInfo)
                        isAdmin || course.courseId in history.courseGroup
                    else
                        isAdmin || checkPoint.checkPointId in history.checkpointGroup
                }

                val isInfoReport = !isInfoRemove && UseCaseResponse.Status.Success == profile.status

                copy(
                    listState = listState.copy(
                        listItemGroup = listItemGroup
                    ),
                    popUpState = popUpState.copy(
                        commentState = popUpState.commentState.copy(
                            commentItemGroup = commentGroup,
                            commentAddState = popUpState.commentState.commentAddState
                        )
                    ),
                    bottomSheetState = bottomSheetState.copy(
                        infoState = bottomSheetState.infoState.copy(
                            isRemoveButton = isInfoRemove,
                            isReportButton = isInfoReport,
                        )
                    )
                )
            }
        }.collect { state ->
            this.value = state
        }
    }

    private fun visibleInitWithLevel(level: Int) {
        when (level) {
            1 -> {//목록
                _driveScreenState.value = _driveScreenState.value.run {
                    mapState.mapOverlayGroup.showCourseMapOverlay()
                    copy(
                        listState = listState.copy(isVisible = true),
                        popUpState = popUpState.copy(isVisible = false),
                        floatingButtonState = DriveScreenState.FloatingButtonState(),
                        bottomSheetState = bottomSheetState.copy(
                            isVisible = false,
                            checkPointAddState = CheckPointAddState()
                        )
                    )
                }
            }

            2 -> {//코스
                _driveScreenState.value = _driveScreenState.value.run {
                    copy(
                        listState = listState.copy(isVisible = false),
                        popUpState = popUpState.copy(isVisible = false),
                        floatingButtonState = DriveScreenState.FloatingButtonState(
                            false, true, true, true, false, true
                        ),
                        bottomSheetState = bottomSheetState.copy(
                            isVisible = false,
                            checkPointAddState = CheckPointAddState()
                        )
                    )
                }
            }

            3 -> {//체크포인트
                _driveScreenState.value = _driveScreenState.value.run {
                    copy(
                        listState = listState.copy(isVisible = false),
                        popUpState = popUpState.copy(
                            isVisible = true,
                            commentState = CommentState()
                        ),
                        floatingButtonState = DriveScreenState.FloatingButtonState(
                            true, false, true, true, false, true
                        ),
                        bottomSheetState = bottomSheetState.copy(
                            isVisible = false,
                            checkPointAddState = CheckPointAddState()
                        )
                    )
                }
            }

            4 -> {//바텀시트
                _driveScreenState.value = _driveScreenState.value.run {
                    copy(
                        listState = listState,
                        popUpState = popUpState,
                        floatingButtonState = floatingButtonState,
                        bottomSheetState = bottomSheetState.copy(isVisible = false)
                    )
                }
            }
        }
    }


    //유틸
    private suspend fun getCommentItemGroup(checkPointId: String): List<CommentItemState> {
        return getCommentForCheckPointUseCase(checkPointId).map {
            val isLike = it.commentId in getHistoryStreamUseCase().first().likeGroup
            CommentItemState(
                data = it,
                isLike = isLike,
                isFold = it.detailedReview.length >= 70
            )
        }
    }

    private suspend fun getNearByListDataGroup(
        center: LatLng,
        meter: Int,
        data: List<Course>
    ): List<DriveScreenState.ListState.ListItemState> {
        return data.mapNotNull {
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
        }.sortedBy { it.course.courseId }.apply {
            coroutineScope {
                this@apply.forEach {
                    launch {
                        getCheckPointForMarkerUseCase(it.course.courseId)// 체크포인트 미리 불러오기
                    }
                }
            }
        }
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
                launch {
                    getImageForPopupUseCase(it.imageName)  // 체크포인트 이미지 미리로드
                }
            }
        }
    }

    private suspend fun List<CheckPoint>.commentPreLoad() {
        coroutineScope {
            forEach {
                launch {
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
}
