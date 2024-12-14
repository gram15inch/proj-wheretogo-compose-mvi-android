package com.wheretogo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheretogo.domain.model.map.CheckPoint
import com.wheretogo.domain.model.map.Comment
import com.wheretogo.domain.model.map.Course
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.model.map.MetaCheckPoint
import com.wheretogo.domain.model.map.OverlayTag
import com.wheretogo.domain.model.map.Viewport
import com.wheretogo.domain.repository.CheckPointRepository
import com.wheretogo.domain.repository.CommentRepository
import com.wheretogo.domain.repository.ImageRepository
import com.wheretogo.domain.repository.UserRepository
import com.wheretogo.domain.toMetaCheckPoint
import com.wheretogo.domain.usecase.GetNearByCourseUseCase
import com.wheretogo.presentation.feature.map.distanceTo
import com.wheretogo.presentation.feature.naver.getMapOverlay
import com.wheretogo.presentation.intent.DriveScreenIntent
import com.wheretogo.presentation.model.MapOverlay
import com.wheretogo.presentation.state.DriveScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DriveViewModel @Inject constructor(
    private val getNearByCourseUseCase: GetNearByCourseUseCase,
    private val userRepository: UserRepository,
    private val checkPointRepository: CheckPointRepository,
    private val imageRepository: ImageRepository,
    private val commentRepository: CommentRepository
) : ViewModel() {
    private val _driveScreenState = MutableStateFlow(DriveScreenState())
    private val _cacheCourseMapOverlayGroup = mutableMapOf<Int, MapOverlay>() // id, hashcode
    private val _cacheCheckPointMapOverlayGroup = mutableMapOf<Int, MapOverlay>() // id, hashcode
    private val _cacheCheckPointGroup = mutableMapOf<Int, List<CheckPoint>>() // id, hashcode

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
            userRepository.getBookmarkFlow().combine(_driveScreenState) { bookmarks, state ->
                val data = state.listState.copy().listItemGroup.map {
                    it.copy(isBookmark = if (it.course.courseId in bookmarks) true else false)
                }
                state.copy(listState = state.listState.copy(listItemGroup = data))
            }.collect {
                _driveScreenState.value = it
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
                is DriveScreenIntent.DriveListItemClick -> driveListItemClick(intent.item)
                is DriveScreenIntent.DriveListItemBookmarkClick -> driveListItemBookmarkClick(intent.item)
                is DriveScreenIntent.CommentListItemClick -> commentListItemClick(intent.comment)
                is DriveScreenIntent.CommentLikeClick -> commentLikeClick(intent.comment)
                is DriveScreenIntent.FoldFloatingButtonClick -> foldFloatingButtonClick()
                is DriveScreenIntent.CommentFloatingButtonClick -> commentFloatingButtonClick()
                is DriveScreenIntent.ExportMapFloatingButtonClick -> exportMapFloatingButtonClick()
            }
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
                isCommentVisible = false
            ),
            floatingButtonState = _driveScreenState.value.floatingButtonState.copy(
                isCommentVisible = false
            )
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
        val image = imageRepository.getImage(checkpoint.remoteImgUrl, "normal")
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
        checkPointGroup.imagePreLoad("normal")
        checkPointGroup.commentPreLoad()
        _driveScreenState.value = _driveScreenState.value.copy(isLoading = false)
    }

    private suspend fun driveListItemBookmarkClick(state: DriveScreenState.ListState.ListItemState) {
        if (state.isBookmark)
            userRepository.removeBookmark(state.course.courseId)
        else
            userRepository.addBookmark(state.course.courseId)

    }

    private fun commentListItemClick(comment: Comment) {
        _driveScreenState.value = _driveScreenState.value.run {
            copy(
                popUpState = popUpState.copy(
                    commentState = popUpState.commentState.copy(commentItemGroup = popUpState.commentState.commentItemGroup.map {
                        if (it.data.commentId == comment.commentId)
                            it.copy(isFold = !it.isFold)
                        else
                            it
                    })
                )
            )
        }
    }

    private fun commentLikeClick(comment: Comment) {
        _driveScreenState.value = _driveScreenState.value.run {
            copy(
                popUpState = popUpState.copy(
                    commentState = popUpState.commentState.copy(commentItemGroup = popUpState.commentState.commentItemGroup.map {
                        if (it.data.commentId == comment.commentId)
                            it.copy(
                                data = it.data.copy(like = it.data.like + if (it.isLike) -1 else +1),
                                isLike = !it.isLike
                            )
                        else
                            it
                    })
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
        val comment = commentRepository.getComment(_driveScreenState.value.popUpState.checkPointId)

        _driveScreenState.value = _driveScreenState.value.run {
            copy(
                popUpState = popUpState.copy(
                    isCommentVisible = !popUpState.isCommentVisible,
                    commentState = popUpState.commentState.copy(
                        commentItemGroup = comment.map {
                            DriveScreenState.PopUpState.CommentState.CommentItemState(
                                data = it
                            )
                        }
                    )
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
                        _cacheCheckPointGroup.getOrPut(it.course.courseId.hashCode()) {
                            checkPointRepository.getCheckPointGroup(it.course.checkpoints.toMetaCheckPoint())// 체크포인트 미리 불러오기
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
                    _cacheCheckPointMapOverlayGroup.getOrPut(checkPoint.checkPointId.hashCode()) {
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
            _cacheCourseMapOverlayGroup.getOrPut(it.courseId.hashCode()) { getMapOverlay(it) }
        }
    }

    private suspend fun getCheckPointGroup(
        courseId: String,
        metaCheckPoint: MetaCheckPoint = MetaCheckPoint()
    ): List<CheckPoint> {
        return _cacheCheckPointGroup.getOrPut(courseId.hashCode()) {
            checkPointRepository.getCheckPointGroup(metaCheckPoint)
        }
    }

    private fun hideCourseMapOverlayWithout(withoutOverlyId: String) {
        _latestCourseMapOverlayGroup.onEach {
            if (it.overlayId.hashCode() != withoutOverlyId.hashCode()) {
                it.marker.isVisible = false
                it.pathOverlay.isVisible = false
            }
        }
    }

    private suspend fun List<CheckPoint>.imagePreLoad(size: String) {
        coroutineScope {
            forEach {
                launch {
                    imageRepository.getImage(it.remoteImgUrl, size)  // 체크포인트 이미지 미리로드
                }
            }
        }
    }

    private suspend fun List<CheckPoint>.commentPreLoad() {
        coroutineScope {
            forEach {
                launch {
                    commentRepository.getComment(it.checkPointId)
                }
            }
        }
    }
}