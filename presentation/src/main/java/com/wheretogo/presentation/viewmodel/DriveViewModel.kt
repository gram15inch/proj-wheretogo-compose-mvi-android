package com.wheretogo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheretogo.domain.model.MarkerTag
import com.wheretogo.domain.model.map.CheckPoint
import com.wheretogo.domain.model.map.Comment
import com.wheretogo.domain.model.map.Journey
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.model.map.Viewport

import com.wheretogo.domain.usecase.GetNearByJourneyUseCase
import com.wheretogo.presentation.exceptions.MapNotInitializedException
import com.wheretogo.presentation.feature.naver.getMapOverlay
import com.wheretogo.presentation.intent.DriveScreenIntent
import com.wheretogo.presentation.model.MapOverlay
import com.wheretogo.presentation.model.getCommentDummy
import com.wheretogo.presentation.state.DriveScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DriveViewModel @Inject constructor(
    private val getNearByJourneyUseCase: GetNearByJourneyUseCase
) : ViewModel() {
    private val _driveScreenState = MutableStateFlow(DriveScreenState())
    private val _cacheCourseMapOverlayGroup = mutableMapOf<Int, MapOverlay>() // code
    private val _cacheCheckPointMapOverlayGroup = mutableMapOf<Int, MapOverlay>() // code
    private val _cacheCheckPointGroup = mutableMapOf<Int, List<CheckPoint>>() // code

    private val _latestCourseMapOverlayGroup = mutableListOf<MapOverlay>()
    private var _latestItemJourney = Journey()
    private var _latestLocation = LatLng()
    private var _latestCamera = LatLng()


    val driveScreenState: StateFlow<DriveScreenState> = _driveScreenState

    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        when (exception) {
            is MapNotInitializedException -> {
                _driveScreenState.value = _driveScreenState.value.copy(
                    error = exception.message
                )
            }

            else -> {
                exception.printStackTrace()
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
                is DriveScreenIntent.DriveListItemClick -> driveListItemClick(intent.journey)
                is DriveScreenIntent.CommentListItemClick -> commentListItemClick(intent.comment)
                is DriveScreenIntent.CommentLikeClick -> commentLikeClick(intent.comment)
                is DriveScreenIntent.FoldFloatingButtonClick -> foldFloatingButtonClick()
                is DriveScreenIntent.CommentFloatingButtonClick -> commentFloatingButtonClick()
                is DriveScreenIntent.ExportMapFloatingButtonClick -> exportMapFloatingButtonClick()
            }
        }
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
        val data: Pair<List<MapOverlay>, List<Journey>> = coroutineScope {
            val mapData = async {
                getNearByJourneyUseCase.byViewport(latLng, viewPort).sortedBy { it.code }
                    .map {
                        _cacheCheckPointGroup[it.code] = it.checkPoints
                        _cacheCourseMapOverlayGroup.getOrPut(it.code) { getMapOverlay(it) }
                    }
            }
            val listData = async { getNearByJourneyUseCase.byDistance(latLng, 2000) }
            Pair(mapData.await(), listData.await())
        }

        if (!_driveScreenState.value.floatingButtonState.isFoldVisible) {
            _driveScreenState.value = _driveScreenState.value.copy(
                mapState = _driveScreenState.value.mapState.copy(
                    mapData = data.first
                ),
                listState = _driveScreenState.value.listState.copy(
                    listData = data.second
                )
            )

            _latestCourseMapOverlayGroup.clear()
            _latestCourseMapOverlayGroup.addAll(data.first)
        }

    }

    private fun updateLocation(latLng: LatLng) {
        _latestLocation = latLng
    }

    private fun courseMarkerClick(tag: MarkerTag) {
        val newMapData =
            _latestCourseMapOverlayGroup + _cacheCheckPointGroup[tag.code]!!.map { checkPoint ->
                _cacheCheckPointMapOverlayGroup.getOrPut(checkPoint.id) {
                    getMapOverlay(tag.code, checkPoint)
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

            val newMapData = _latestCourseMapOverlayGroup - _latestItemJourney.checkPoints.map {
                _cacheCheckPointMapOverlayGroup.getOrPut(it.id) {
                    getMapOverlay(_latestItemJourney.code, it)
                }.apply {
                    this.marker.isVisible = false
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

    private fun driveListItemClick(journey: Journey) {
        _latestItemJourney = journey
        _latestCourseMapOverlayGroup.hideCourseMapOverlayWithout(journey.code)
        val newMapData = _latestCourseMapOverlayGroup + journey.checkPoints.map {
            _cacheCheckPointMapOverlayGroup.getOrPut(it.id) { getMapOverlay(journey.code, it) }
                .apply {
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
                    clickItem = journey
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
            if (this[idx].code != withoutCode)
                _cacheCourseMapOverlayGroup[this[idx].code]?.apply {
                    this.marker.isVisible = false
                    this.pathOverlay.isVisible = false
                }
        }
    }

    private fun hideCourseMapOverlayWithout(withoutCode: Int) {
        _cacheCourseMapOverlayGroup.forEach {
            if (it.value.code != withoutCode)
                it.apply {
                    it.value.marker.isVisible = false
                    it.value.pathOverlay.isVisible = false
                }
        }
    }
}