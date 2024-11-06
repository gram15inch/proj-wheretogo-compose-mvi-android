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
    private val _isResetOverlay = MutableStateFlow<Boolean>(false)
    private val _cacheOverlayGroup = mutableMapOf<Int, MapOverlay>()

    private val _latestMapOverlayGroup = mutableListOf<MapOverlay>()
    private var _latestJourney = Journey()
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
        }
    }

    fun handleIntent(intent: DriveScreenIntent) {
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

    val isRefreshOverlay: StateFlow<Boolean> = _isResetOverlay


    private fun mapIsReady() {
        viewModelScope.launch(exceptionHandler) {
            _driveScreenState.value = _driveScreenState.value.copy(
                mapState = _driveScreenState.value.mapState.copy(isMapReady = true)
            )
        }
    }

    private fun moveToCurrentLocation() {
        viewModelScope.launch(exceptionHandler) {

        }
    }

    private fun updateCamara(latLng: LatLng, viewPort: Viewport) {
        viewModelScope.launch(exceptionHandler) {
            _latestCamera = latLng
            val data: Pair<List<MapOverlay>, List<Journey>> = coroutineScope {
                val mapData = async {
                    getNearByJourneyUseCase.byViewport(latLng, viewPort).sortedBy { it.code }.map {
                        _cacheOverlayGroup.getOrPut(it.code) { getMapOverlay(it) }
                    }
                }
                val listData = async { getNearByJourneyUseCase.byDistance(latLng, 1500) }
                Pair(mapData.await(), listData.await())
            }

            _driveScreenState.value = _driveScreenState.value.copy(
                mapState = _driveScreenState.value.mapState.copy(
                    mapData = data.first
                ),
                listState = _driveScreenState.value.listState.copy(
                    listData = data.second
                )
            )
        }
    }

    private fun updateLocation(latLng: LatLng) {
        viewModelScope.launch(exceptionHandler) {
            _latestLocation = latLng
        }
    }

    private fun markerClick(code: Int) {
        viewModelScope.launch {

        }
    }

    private fun floatingButtonClick() {
        _cacheOverlayGroup.forEach {
            if (it.key >= 1000) {
                it.value.pathOverlay.isVisible = true
                it.value.marker.isVisible = true
            }
        }
        val newMapData = _latestMapOverlayGroup - _latestJourney.checkPoints.map {
            _cacheOverlayGroup.getOrPut(it.id) { getMapOverlay(it) }.apply {
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

    private fun listItemClick(journey: Journey) {
        viewModelScope.launch(exceptionHandler) {
            _latestJourney = journey
            _driveScreenState.value.mapState.mapData.apply {
                for (idx in this.indices) {
                    if (this[idx].code != journey.code)
                        _cacheOverlayGroup[this[idx].code]?.apply {
                            this.marker.isVisible = false
                            this.pathOverlay.isVisible = false
                        }
                }
            }
            val newMapData = _latestMapOverlayGroup.apply {
                for (idx in this.indices) {
                    if (this[idx].code != journey.code) {
                        this[idx].marker.isVisible = false
                        this[idx].pathOverlay.isVisible = false
                    }
                }
            } + journey.checkPoints.map {
                _cacheOverlayGroup.getOrPut(it.id) { getMapOverlay(it) }.apply {
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

    private fun hideCheckPointOverlay(id: Int) {
        _cacheOverlayGroup.get(id)?.apply {
            this.marker.isVisible = false
        }
    }

    private fun hideCourseOverlay(code: Int) {
        _cacheOverlayGroup.get(code)?.apply {
            this.marker.isVisible = false
            this.pathOverlay.isVisible = false
        }
    }
}