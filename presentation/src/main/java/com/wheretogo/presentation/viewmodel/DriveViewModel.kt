package com.wheretogo.presentation.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheretogo.domain.getCheckPointMarkerTag
import com.wheretogo.domain.model.map.CheckPoint
import com.wheretogo.domain.model.map.Comment
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.model.map.MarkerTag
import com.wheretogo.domain.model.map.Viewport
import com.wheretogo.domain.repository.CheckPointRepository
import com.wheretogo.domain.repository.CourseRepository
import com.wheretogo.domain.repository.UserRepository
import com.wheretogo.domain.toMetaCheckPoint
import com.wheretogo.domain.usecase.GetNearByCourseUseCase
import com.wheretogo.presentation.feature.naver.getMapOverlay
import com.wheretogo.presentation.getCommentDummy
import com.wheretogo.presentation.intent.DriveScreenIntent
import com.wheretogo.presentation.model.MapOverlay
import com.wheretogo.presentation.state.DriveScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
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
    private val courseRepository: CourseRepository,
    private val checkPointRepository: CheckPointRepository,
) : ViewModel() {
    private val _driveScreenState = MutableStateFlow(DriveScreenState())
    private val _cacheCourseMapOverlayGroup = mutableMapOf<Int, MapOverlay>() // id, hashcode
    private val _cacheCheckPointMapOverlayGroup = mutableMapOf<Int, MapOverlay>() // id, hashcode
    private val _cacheCheckPointGroup = mutableMapOf<Int, List<CheckPoint>>() // id, hashcode

    private val _latestCourseMapOverlayGroup = mutableListOf<MapOverlay>()
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

    private suspend fun driveListItemBookmarkClick(state: DriveScreenState.ListState.ListItemState) {
        if (state.isBookmark)
            userRepository.removeBookmark(state.course.courseId)
        else
            userRepository.addBookmark(state.course.courseId)

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

    private fun mapIsReady() {
        _driveScreenState.value = _driveScreenState.value.copy(
            mapState = _driveScreenState.value.mapState.copy(isMapReady = true)
        )
    }

    private suspend fun updateCamara(latLng: LatLng, viewPort: Viewport) {
        _latestCamera = latLng

        val data = getNearByCourseUseCase(latLng)
        val mapData = data.map {
            //_cacheCheckPointGroup[it.courseId ] = it.checkPoints
            _cacheCourseMapOverlayGroup.getOrPut(it.courseId.hashCode()) { getMapOverlay(it) }
        }

        val listData = data.mapNotNull {
            val camera = latLng
            val course = it.cameraLatLng
            val distance = FloatArray(1).apply {
                Location.distanceBetween(
                    camera.latitude,
                    camera.longitude,
                    course.latitude,
                    course.longitude,
                    this
                )
            }[0].toInt()
            if (distance < 3000)
                DriveScreenState.ListState.ListItemState(
                    distanceFromCenter = distance,
                    course = it,
                    isBookmark = false
                )
            else
                null
        }.sortedBy { it.course.courseId }

        if (!_driveScreenState.value.floatingButtonState.isFoldVisible) {
            _driveScreenState.value = _driveScreenState.value.copy(
                mapState = _driveScreenState.value.mapState.copy(
                    mapData = mapData
                ),
                listState = _driveScreenState.value.listState.copy(
                    listItemGroup = listData
                )
            )

            _latestCourseMapOverlayGroup.clear()
            _latestCourseMapOverlayGroup.addAll(mapData)
        }

    }

    private fun updateLocation(latLng: LatLng) {
        _latestLocation = latLng
    }

    private fun courseMarkerClick(tag: MarkerTag) {
        val mapData =
            _latestCourseMapOverlayGroup + _cacheCheckPointGroup[tag.code]!!.map { checkPoint ->
                _cacheCheckPointMapOverlayGroup.getOrPut(getCheckPointMarkerTag(checkPoint.checkPointId)) {
                    getMapOverlay(checkPoint)
                }.apply {
                    this.marker.isVisible = true
                }
            }
        _driveScreenState.value = _driveScreenState.value.run {
            copy(
                mapState = mapState.copy(
                    mapData = mapData
                ),
                listState = listState.copy(
                    isVisible = false
                ),
                floatingButtonState = floatingButtonState.copy(
                    isFoldVisible = true,
                    isExportVisible = false
                )
            )
        }
    }

    private fun checkPointMarkerClick(tag: MarkerTag) {
        _driveScreenState.value = _driveScreenState.value.run {
            copy(
                listState = listState.copy(
                    isVisible = false
                ),
                popUpState = popUpState.copy(
                    isVisible = true,
                    checkPointId = tag.id,
                    imageUrl = "/data/user/0/com.dhkim139.wheretogo/cache/thumbnails/photo_original_768x1024_70.jpg"
                    //tag = _cacheCheckPointGroup[tag.code]!!.first{it.id==tag.id}.url
                ),
                floatingButtonState = floatingButtonState.copy(
                    isFoldVisible = true,
                    isCommentVisible = true,
                    isBackPlateVisible = false,
                )
            )
        }
    }

    private suspend fun foldFloatingButtonClick() {
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
                _latestCourseMapOverlayGroup - _latestItemState.course.checkpoints.map {
                    _cacheCheckPointMapOverlayGroup.getOrPut(it.checkPointId.hashCode()) {
                        getMapOverlay(it)
                    }.apply {
                        marker.isVisible = false
                    }
                }.toSet()

            _driveScreenState.value = _driveScreenState.value.run {
                copy(
                    mapState = mapState.copy(
                        mapData = newMapData
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

    private fun commentFloatingButtonClick() {
        _driveScreenState.value = _driveScreenState.value.run {
            copy(
                popUpState = popUpState.copy(
                    isCommentVisible = !popUpState.isCommentVisible,
                    //todo 임시
                    commentState = popUpState.commentState.copy(data = getCommentDummy())
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

    private suspend fun driveListItemClick(state: DriveScreenState.ListState.ListItemState) {
        _latestItemState = state
        _latestCourseMapOverlayGroup.hideCourseMapOverlayWithout(state.course.courseId.hashCode())
        val checkPoints =
            checkPointRepository.getCheckPointGroup(state.course.checkpoints.toMetaCheckPoint())

        _cacheCheckPointGroup[state.course.courseId.hashCode()] = checkPoints
        val newMapData =
            _latestCourseMapOverlayGroup + checkPoints.map {
                _cacheCheckPointMapOverlayGroup.getOrPut(it.checkPointId.hashCode()) {
                    getMapOverlay(it)
                }.apply {
                    this.marker.isVisible = true
                }
            }
        _driveScreenState.value = _driveScreenState.value.run {
            copy(
                mapState = mapState.copy(
                    mapData = newMapData
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
    }

    private fun commentListItemClick(comment: Comment) {
        _driveScreenState.value = _driveScreenState.value.run {
            copy(
                popUpState = popUpState.copy(
                    commentState = popUpState.commentState.copy(data = popUpState.commentState.data.map {
                        if (it.commentId == comment.commentId)
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
                    commentState = popUpState.commentState.copy(data = popUpState.commentState.data.map {
                        if (it.commentId == comment.commentId)
                            it.copy(like = it.like + if (it.isLike) -1 else +1, isLike = !it.isLike)
                        else
                            it
                    })
                )
            )
        }
    }

    private fun List<MapOverlay>.hideCourseMapOverlayWithout(withoutCode: Int) {
        for (idx in this.indices) {
            if (this[idx].overlayId.hashCode() != withoutCode)
                _cacheCourseMapOverlayGroup[this[idx].overlayId.hashCode()]?.apply {
                    this.marker.isVisible = false
                    this.pathOverlay.isVisible = false
                }
        }
    }

    private fun hideCourseMapOverlayWithout(withoutCode: Int) {
        _cacheCourseMapOverlayGroup.forEach {
            if (it.value.overlayId.hashCode() != withoutCode)
                it.apply {
                    it.value.marker.isVisible = false
                    it.value.pathOverlay.isVisible = false
                }
        }
    }
}