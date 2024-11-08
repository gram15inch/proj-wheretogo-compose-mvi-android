package com.wheretogo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheretogo.domain.model.CheckPoint
import com.wheretogo.domain.model.Journey
import com.wheretogo.domain.model.LatLng
import com.wheretogo.domain.model.Viewport
import com.wheretogo.domain.usecase.GetNearByJourneyUseCase
import com.wheretogo.presentation.exceptions.MapNotInitializedException
import com.wheretogo.presentation.feature.naver.getMapOverlay
import com.wheretogo.presentation.intent.DriveScreenIntent
import com.wheretogo.presentation.model.MapOverlay
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
                is DriveScreenIntent.MapIsReady -> mapIsReady()
                is DriveScreenIntent.MoveToCurrentLocation -> moveToCurrentLocation()
                is DriveScreenIntent.UpdateCamera -> updateCamara(intent.latLng, intent.viewPort)
                is DriveScreenIntent.UpdateLocation -> updateLocation(intent.latLng)
                is DriveScreenIntent.MarkerClick -> markerClick(intent.code)
                is DriveScreenIntent.ListItemClick -> listItemClick(intent.journey)
                is DriveScreenIntent.FloatingButtonClick -> floatingButtonClick()
            }
        }
    }

    private fun mapIsReady() {
        _driveScreenState.value = _driveScreenState.value.copy(
            mapState = _driveScreenState.value.mapState.copy(isMapReady = true)
        )
    }

    private fun moveToCurrentLocation() {

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
            val listData = async { getNearByJourneyUseCase.byDistance(latLng, 1500) }
            Pair(mapData.await(), listData.await())
        }

        if (!_driveScreenState.value.floatingButtonState.isVisible) {
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

    private fun markerClick(code: Int) {
        val newMapData = _latestCourseMapOverlayGroup + _cacheCheckPointGroup[code]!!.map {
            _cacheCheckPointMapOverlayGroup.getOrPut(it.id) { getMapOverlay(it) }.apply {
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
                    isVisible = true
                )
            )
        }
    }

    private suspend fun floatingButtonClick() {
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
                _cacheCheckPointMapOverlayGroup.getOrPut(it.id) { getMapOverlay(it) }.apply {
                    this.marker.isVisible = false
                }
            }.toSet()

            viewModelScope.launch(exceptionHandler) {
                _driveScreenState.value = _driveScreenState.value.run {
                    copy(
                        mapState = mapState.copy(
                            mapData = newMapData
                        ),
                        listState = listState.copy(
                            isVisible = true
                        ),
                        floatingButtonState = floatingButtonState.copy(
                            isVisible = false
                        )
                    )
                }
            }
        }
    }

    private fun listItemClick(journey: Journey) {
        viewModelScope.launch(exceptionHandler) {
            _latestItemJourney = journey
            _latestCourseMapOverlayGroup.hideCourseMapOverlayWithout(journey.code)
            val newMapData = _latestCourseMapOverlayGroup + journey.checkPoints.map {
                _cacheCheckPointMapOverlayGroup.getOrPut(it.id) { getMapOverlay(it) }.apply {
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
                        isVisible = true
                    )
                )
            }
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