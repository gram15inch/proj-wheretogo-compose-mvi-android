package com.wheretogo.presentation.viewmodel

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheretogo.domain.HistoryType
import com.wheretogo.domain.model.dummy.getEmogiDummy
import com.wheretogo.domain.model.map.CheckPoint
import com.wheretogo.domain.model.map.Course
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.model.map.MetaCheckPoint
import com.wheretogo.domain.model.map.OverlayTag
import com.wheretogo.domain.model.map.Viewport
import com.wheretogo.domain.toMetaCheckPoint
import com.wheretogo.domain.usecase.community.AddCommentToCheckPointUseCase
import com.wheretogo.domain.usecase.community.GetCommentForCheckPointUseCase
import com.wheretogo.domain.usecase.community.RemoveCommentToCheckPointUseCase
import com.wheretogo.domain.usecase.community.ReportCommentUseCase
import com.wheretogo.domain.usecase.map.GetCheckPointForMarkerUseCase
import com.wheretogo.domain.usecase.map.GetImageForPopupUseCase
import com.wheretogo.domain.usecase.map.GetNearByCourseUseCase
import com.wheretogo.domain.usecase.user.GetHistoryStreamUseCase
import com.wheretogo.domain.usecase.user.RemoveHistoryUseCase
import com.wheretogo.domain.usecase.user.UpdateHistoryUseCase
import com.wheretogo.presentation.CommentType
import com.wheretogo.presentation.feature.map.distanceTo
import com.wheretogo.presentation.feature.naver.getMapOverlay
import com.wheretogo.presentation.intent.DriveScreenIntent
import com.wheretogo.presentation.model.MapOverlay
import com.wheretogo.presentation.state.DriveScreenState
import com.wheretogo.presentation.state.DriveScreenState.PopUpState.CommentState.CommentAddState
import com.wheretogo.presentation.state.DriveScreenState.PopUpState.CommentState.CommentItemState
import com.wheretogo.presentation.toComment
import com.wheretogo.presentation.toCommentAddState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class DriveViewModel @Inject constructor(
    private val getNearByCourseUseCase: GetNearByCourseUseCase,
    private val getCheckPointForMarkerUseCase: GetCheckPointForMarkerUseCase,
    private val getCommentForCheckPointUseCase: GetCommentForCheckPointUseCase,
    private val addCommentToCheckPointUseCase: AddCommentToCheckPointUseCase,
    private val removeCommentToCheckPointUseCase: RemoveCommentToCheckPointUseCase,
    private val reportCommentUseCase: ReportCommentUseCase,
    private val getImageForPopupUseCase: GetImageForPopupUseCase,
    private val updateHistoryUseCase: UpdateHistoryUseCase,
    private val removeHistoryUseCase: RemoveHistoryUseCase,
    private val getHistoryStreamUseCase: GetHistoryStreamUseCase
) : ViewModel() {
    private val _driveScreenState = MutableStateFlow(DriveScreenState())
    private val _cacheCourseMapOverlayGroup = mutableMapOf<String, MapOverlay>() // courseId
    private val _cacheCheckPointMapOverlayGroup = mutableMapOf<String, MapOverlay>() // courseId
    private val _cacheCheckPointGroup = mutableMapOf<String, List<CheckPoint>>() // courseId

    private val _latestCourseMapOverlayGroup = mutableSetOf<MapOverlay>()
    private var _latestItemState = DriveScreenState.ListState.ListItemState()
    private var _latestLocation = LatLng()
    private var _latestCamera = LatLng()


    val driveScreenState: StateFlow<DriveScreenState> = _driveScreenState

    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        when (exception) {
            else -> {
                _driveScreenState.value = _driveScreenState.value.copy(
                    error = exception.message
                )
                exception.printStackTrace()
            }
        }
    }

    init {
        viewModelScope.launch(exceptionHandler) {
            combine(driveScreenState, getHistoryStreamUseCase()) { state, history ->

                state.run {
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

                    copy(
                        listState = listState.copy(
                            listItemGroup = listItemGroup
                        ),
                        popUpState = popUpState.copy(
                            commentState = popUpState.commentState.copy(
                                commentItemGroup = commentGroup,
                                commentAddState = popUpState.commentState.commentAddState
                            )
                        )
                    )
                }
            }.collect { state ->
                _driveScreenState.value = state
            }
        }

    }


    fun handleIntent(intent: DriveScreenIntent) {
        viewModelScope.launch(exceptionHandler) {
            when (intent) {
                //결과
                is DriveScreenIntent.MapIsReady -> mapIsReady()
                is DriveScreenIntent.UpdateCamera -> updateCamara(intent.latLng, intent.viewPort)
                is DriveScreenIntent.UpdateLocation -> updateLocation(intent.latLng)
                is DriveScreenIntent.DismissPopup -> dismissPopup()
                is DriveScreenIntent.OverlayRenderComplete -> overlayRenderComplete(intent.isRendered)

                //동작
                is DriveScreenIntent.CourseMarkerClick -> courseMarkerClick(intent.tag)
                is DriveScreenIntent.CheckPointMarkerClick -> checkPointMarkerClick(intent.tag)
                is DriveScreenIntent.DriveListItemClick -> driveListItemClick(intent.itemState)
                is DriveScreenIntent.DriveListItemBookmarkClick -> driveListItemBookmarkClick(intent.itemState)
                is DriveScreenIntent.CommentListItemClick -> commentListItemClick(intent.itemState)
                is DriveScreenIntent.CommentListItemLongClick -> commentListItemLongClick(intent.itemState)
                is DriveScreenIntent.CommentLikeClick -> commentLikeClick(intent.itemState)
                is DriveScreenIntent.CommentAddClick -> commentAddClick(intent.itemState)
                is DriveScreenIntent.CommentRemoveClick -> commentRemoveClick(intent.itemState)
                is DriveScreenIntent.CommentReportClick -> commentReportClick(intent.itemState)
                is DriveScreenIntent.CommentEditValueChange -> commentEditValueChange(intent.textFiled)
                is DriveScreenIntent.CommentEmogiPress -> commentEmogiPress(intent.emogi)
                is DriveScreenIntent.CommentTypePress -> commentTypePress(intent.type)
                is DriveScreenIntent.FoldFloatingButtonClick -> foldFloatingButtonClick()
                is DriveScreenIntent.CommentFloatingButtonClick -> commentFloatingButtonClick()
                is DriveScreenIntent.ExportMapFloatingButtonClick -> exportMapFloatingButtonClick()
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


    // 결과

    private fun mapIsReady() {
        _driveScreenState.value = _driveScreenState.value.copy(
            mapState = _driveScreenState.value.mapState.copy(isMapReady = true)
        )
    }

    private suspend fun updateCamara(latLng: LatLng, viewPort: Viewport) {
        if (_latestCamera.distanceTo(latLng) >= 1) {
            _latestCamera = latLng
            val courseGroup = getNearByCourseUseCase(latLng)
            val overlayGroup = getOverlayGroup(courseGroup)
            val listItemGroup = getNearByListDataGroup(center = latLng, meter = 3000, courseGroup)

            if (!_driveScreenState.value.floatingButtonState.isFoldVisible) {
                _driveScreenState.value = _driveScreenState.value.copy(
                    mapState = _driveScreenState.value.mapState.copy(
                        mapOverlayGroup = overlayGroup
                    ),
                    listState = _driveScreenState.value.listState.copy(
                        listItemGroup = listItemGroup
                    )
                )
            }
            _latestCourseMapOverlayGroup.clear()
            _latestCourseMapOverlayGroup.addAll(overlayGroup)
        }
    }

    private fun updateLocation(latLng: LatLng) {
        _latestLocation = latLng
    }

    private fun dismissPopup() {
        _driveScreenState.value = _driveScreenState.value.copy(
            popUpState = _driveScreenState.value.popUpState.copy(
                isVisible = false,
                commentState = _driveScreenState.value.popUpState.commentState.copy(
                    isCommentVisible = false
                )
            ),
            floatingButtonState = _driveScreenState.value.floatingButtonState.copy(
                isCommentVisible = false
            ),
        )
    }


    private fun overlayRenderComplete(isRendered: Boolean) {
        _driveScreenState.value = _driveScreenState.value.copy(isLoading = !isRendered)
    }

    // 동작

    private suspend fun courseMarkerClick(tag: OverlayTag) {

    }

    private suspend fun checkPointMarkerClick(tag: OverlayTag) {
        val checkpoint = getCheckPointGroup(tag.parentId).first { it.checkPointId == tag.overlayId }
        val image = getImageForPopupUseCase(checkpoint.remoteImgUrl)
        _driveScreenState.value = _driveScreenState.value.run {
            copy(
                listState = listState.copy(
                    isVisible = false
                ),
                popUpState = popUpState.copy(
                    isVisible = true,
                    checkPointId = checkpoint.checkPointId,
                    localImageUrl = image,
                ),
                floatingButtonState = floatingButtonState.copy(
                    isFoldVisible = true,
                    isCommentVisible = true,
                    isBackPlateVisible = false,
                )
            )
        }
    }

    private suspend fun driveListItemClick(state: DriveScreenState.ListState.ListItemState) {
        _driveScreenState.value = _driveScreenState.value.copy(isLoading = true)
        val course = state.course
        _latestItemState = state
        hideCourseMapOverlayWithout(course.courseId)

        val checkPointGroup =
            getCheckPointGroup(course.courseId, course.checkpoints.toMetaCheckPoint())
        val overlayGroup = getOverlayGroup(course.courseId, checkPointGroup, true)

        _driveScreenState.value = _driveScreenState.value.run {
            copy(
                mapState = mapState.copy(
                    mapOverlayGroup = overlayGroup
                ),
                listState = listState.copy(
                    isVisible = false,
                    clickItem = state
                ),
                floatingButtonState = floatingButtonState.copy(
                    isFoldVisible = true,
                    isExportVisible = true
                )
            )
        }
        checkPointGroup.imagePreLoad()
        checkPointGroup.commentPreLoad()
        _driveScreenState.value = _driveScreenState.value.copy(isLoading = false)
    }

    private suspend fun driveListItemLongClick(state: DriveScreenState.ListState.ListItemState) {


        _driveScreenState.value = _driveScreenState.value.run {
            copy(
                mapState = mapState.copy(
                    //  mapOverlayGroup = overlayGroup
                ),
                listState = listState.copy(
                    isVisible = false,
                    clickItem = state
                ),
                floatingButtonState = floatingButtonState.copy(
                    isFoldVisible = true,
                    isExportVisible = true
                )
            )
        }
        _driveScreenState.value = _driveScreenState.value.copy(isLoading = false)
    }


    private suspend fun driveListItemBookmarkClick(itemState: DriveScreenState.ListState.ListItemState) {
        if (itemState.isBookmark)
            removeHistoryUseCase(itemState.course.courseId, HistoryType.BOOKMARK)
        else
            updateHistoryUseCase(itemState.course.courseId, HistoryType.BOOKMARK)

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
        if (itemState.isLike)
            removeHistoryUseCase(itemState.data.commentId, HistoryType.LIKE)
        else {
            updateHistoryUseCase(itemState.data.commentId, HistoryType.LIKE)
        }

        val newCommentStaetGroup =
            _driveScreenState.value.popUpState.commentState.commentItemGroup.map {
                if (it.data.commentId == itemState.data.commentId)
                    it.copy(data = it.data.copy(like = it.data.like + if (itemState.isLike) -1 else 1))
                else
                    it
            }

        _driveScreenState.value = _driveScreenState.value.run {
            copy(
                popUpState = popUpState.copy(
                    commentState = popUpState.commentState.copy(commentItemGroup = newCommentStaetGroup)
                )
            )
        }

        addCommentToCheckPointUseCase(commentGroup = newCommentStaetGroup.map { it.data })
    }


    private suspend fun commentAddClick(itemState: CommentAddState) {
        val comment = itemState.toComment().copy(commentId = UUID.randomUUID().toString())
        addCommentToCheckPointUseCase(comment)
        _driveScreenState.value = _driveScreenState.value.run {
            this.copy(
                popUpState = popUpState.copy(
                    commentState = popUpState.commentState.copy(
                        commentAddState = comment.toCommentAddState(),
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

    private suspend fun commentRemoveClick(itemState: CommentItemState) {
        removeCommentToCheckPointUseCase(itemState.data)
        val checkPointId = _driveScreenState.value.popUpState.checkPointId
        val commentItemState =
            getCommentForCheckPointUseCase(checkPointId).map {
                val isLike = it.commentId in getHistoryStreamUseCase().first().likeGroup
                CommentItemState(
                    data = it,
                    isLike = isLike,
                    isFold = it.detailedReview.length >= 70
                )
            }
        _driveScreenState.value = _driveScreenState.value.run {
            this.copy(
                popUpState = popUpState.copy(
                    commentState = popUpState.commentState.copy(
                        isCommentSettingVisible = false,
                        selectedCommentSettingItem = CommentItemState(),
                        commentItemGroup = commentItemState
                    )
                )
            )
        }
    }

    private suspend fun commentReportClick(itemState: CommentItemState) {

        reportCommentUseCase(itemState.data)
        _driveScreenState.value = _driveScreenState.value.run {
            this.copy(
                popUpState = popUpState.copy(
                    commentState = popUpState.commentState.copy(
                        isCommentSettingVisible = false,
                        selectedCommentSettingItem = CommentItemState()
                    )
                )
            )
        }
    }


    private suspend fun foldFloatingButtonClick() {
        val course = _latestItemState.course
        coroutineScope {
            launch {
                _latestCourseMapOverlayGroup.forEach {
                    it.pathOverlay.isVisible = true
                    it.marker.isVisible = true
                }
            }
            launch {
                _cacheCheckPointMapOverlayGroup.forEach {
                    it.value.marker.isVisible = false
                }
            }

            val newMapData =
                _latestCourseMapOverlayGroup - getOverlayGroup(
                    course.courseId,
                    course.checkpoints,
                    false
                ).toSet()

            _driveScreenState.value = _driveScreenState.value.run {
                copy(
                    mapState = mapState.copy(
                        mapOverlayGroup = newMapData
                    ),
                    listState = listState.copy(
                        isVisible = true
                    ),
                    popUpState = popUpState.copy(
                        isVisible = false
                    ),
                    floatingButtonState = floatingButtonState.copy(
                        isCommentVisible = false,
                        isExportVisible = false,
                        isFoldVisible = false,
                        isBackPlateVisible = false
                    )
                )
            }

        }
    }

    private suspend fun commentFloatingButtonClick() {
        val checkPointId = _driveScreenState.value.popUpState.checkPointId
        val commentItemState =
            getCommentForCheckPointUseCase(checkPointId).map {
                val isLike = it.commentId in getHistoryStreamUseCase().first().likeGroup
                CommentItemState(
                    data = it,
                    isLike = isLike,
                    isFold = it.detailedReview.length >= 70
                )
            }
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

    private fun exportMapFloatingButtonClick() {
        _driveScreenState.value = _driveScreenState.value.run {
            copy(
                floatingButtonState = floatingButtonState.copy(
                    isBackPlateVisible = !floatingButtonState.isBackPlateVisible
                )
            )
        }
    }


    // 유틸

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
                        _cacheCheckPointGroup.getOrPut(it.course.courseId) {
                            getCheckPointForMarkerUseCase(it.course.checkpoints.toMetaCheckPoint())// 체크포인트 미리 불러오기
                        }
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
        return _latestCourseMapOverlayGroup + coroutineScope {
            checkPoints.map { checkPoint ->
                async {
                    _cacheCheckPointMapOverlayGroup.getOrPut(checkPoint.checkPointId) {
                        getMapOverlay(courseId, checkPoint)
                    }.apply {
                        this.marker.isVisible = visible
                    }
                }
            }.awaitAll()
        }
    }

    private fun getOverlayGroup(courseGroup: List<Course>): Set<MapOverlay> {
        return _latestCourseMapOverlayGroup + courseGroup.map {
            _cacheCourseMapOverlayGroup.getOrPut(it.courseId) { getMapOverlay(it) }
        }
    }

    private suspend fun getCheckPointGroup(
        courseId: String,
        metaCheckPoint: MetaCheckPoint = MetaCheckPoint()
    ): List<CheckPoint> {
        return _cacheCheckPointGroup.getOrPut(courseId) {
            getCheckPointForMarkerUseCase(metaCheckPoint)
        }
    }

    private fun hideCourseMapOverlayWithout(withoutOverlyId: String) {
        _latestCourseMapOverlayGroup.onEach {
            if (it.overlayId != withoutOverlyId) {
                it.marker.isVisible = false
                it.pathOverlay.isVisible = false
            }
        }
    }

    private suspend fun List<CheckPoint>.imagePreLoad() {
        coroutineScope {
            forEach {
                launch {
                    getImageForPopupUseCase(it.remoteImgUrl)  // 체크포인트 이미지 미리로드
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
}