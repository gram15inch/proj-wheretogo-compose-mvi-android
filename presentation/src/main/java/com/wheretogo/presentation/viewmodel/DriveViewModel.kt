package com.wheretogo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheretogo.domain.model.CheckPoint
import com.wheretogo.domain.model.Journey
import com.wheretogo.domain.model.LatLng
import com.wheretogo.domain.model.Viewport
import com.wheretogo.domain.usecase.GetJourneyUseCase
import com.wheretogo.domain.usecase.GetNearByJourneyUseCase
import com.wheretogo.presentation.feature.naver.getMapOverlay
import com.wheretogo.presentation.model.MapOverlay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DriveViewModel @Inject constructor(
    private val getJourneyUseCase: GetJourneyUseCase,
    private val getNearByJourneyUseCase: GetNearByJourneyUseCase
) : ViewModel() {
    private val _journeyGroupInCenter  = MutableStateFlow<List<Journey>>(emptyList())
    private val _journeyGroup  = MutableStateFlow<List<Journey>>(emptyList())
    private val _cacheOverlayGroup = mutableMapOf<Int,MapOverlay>()
    private val _visibleOverlayGroup = MutableStateFlow<MutableMap<Int,MapOverlay>>(mutableMapOf())
    private val _isResetOverlay    = MutableStateFlow<Boolean>(false)

    val journeyGroup  : StateFlow<List<Journey>> = _journeyGroup
    val visibleOverlayGroup : StateFlow<MutableMap<Int,MapOverlay>> = _visibleOverlayGroup

    val isRefreshOverlay    : StateFlow<Boolean> = _isResetOverlay
    init {
        viewModelScope.launch {
            launch {
                _journeyGroupInCenter.collect {
                    _isResetOverlay.value = true
                    _journeyGroup.value=it
                }
            }
        }
    }

    fun fetchNearByJourneyInMap(current: LatLng, viewPort: Viewport) {
        viewModelScope.launch {
            _visibleOverlayGroup.value = getNearByJourneyUseCase.byViewport(current, viewPort)
                .sortedBy { it.code }
                .associateBy (
                    { it.code }, { _cacheOverlayGroup.getOrPut(it.code){ getMapOverlay(it)} }
                ).toMutableMap()
        }
    }

    fun fetchNearByJourneyInList(latLng: LatLng) {
        viewModelScope.launch {
            _journeyGroupInCenter.value = getNearByJourneyUseCase.byDistance(latLng,1500)
        }
    }

    fun resetJourneyGroup(){
        viewModelScope.launch {
            _journeyGroup.value = _journeyGroupInCenter.value
            _isResetOverlay.value = false
        }
    }

    fun hideJourneyWithoutItem(itemCode:Int){
        viewModelScope.launch {
            if(_journeyGroup.value.size==1 && _journeyGroup.value.first().code==itemCode)
                resetJourneyGroup()
            else
                _journeyGroup.value = _journeyGroup.value.filter { it.code == itemCode }
        }
    }

    fun showCheckPointOverlay(item:CheckPoint){
        viewModelScope.launch {
            val overlay = _cacheOverlayGroup.getOrPut(item.id){ getMapOverlay(item)}.apply {
                marker.isVisible= true
            }
            _visibleOverlayGroup.value.putIfAbsent(item.id, overlay)
        }
    }

    fun hideCheckPointOverlay(item: CheckPoint){
        viewModelScope.launch {
            _cacheOverlayGroup.get(item.id)?.apply {
                this.marker.isVisible=false
            }
        }
    }
}