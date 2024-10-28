package com.wheretogo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheretogo.domain.model.Course
import com.wheretogo.domain.model.Journey
import com.wheretogo.domain.model.LatLng
import com.wheretogo.domain.model.Viewport
import com.wheretogo.domain.usecase.GetJourneyUseCase
import com.wheretogo.domain.usecase.GetNearByJourneyUseCase
import com.wheretogo.presentation.feature.naver.getJourneyOverlay
import com.wheretogo.presentation.model.JourneyOverlay
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
    private val _journeyGroupInViewport   = MutableStateFlow<Set<Journey>>(emptySet())
    private val _journeyGroupInCenter  = MutableStateFlow<List<Journey>>(emptyList())
    private val _journeyGroup  = MutableStateFlow<List<Journey>>(emptyList())
    private val _journeyOverlayGroup = MutableStateFlow<MutableMap<Int,JourneyOverlay>>(mutableMapOf())
    private val _isRefreshOverlay    = MutableStateFlow<Boolean>(false)

    val journeyGroupInViewport   : StateFlow<Set<Journey>> = _journeyGroupInViewport
    val journeyGroup  : StateFlow<List<Journey>> = _journeyGroup
    val journeyOverlayGroup : StateFlow<MutableMap<Int,JourneyOverlay>> = _journeyOverlayGroup
    val isRefreshOverlay    : StateFlow<Boolean> = _isRefreshOverlay

    init {
        viewModelScope.launch {
            launch {
                journeyGroupInViewport.collect {
                    it.forEach {
                        journeyOverlayGroup.value.putIfAbsent(it.code, getJourneyOverlay(it))
                    }
                }
            }
            launch {
                _journeyGroupInCenter.collect {
                    _isRefreshOverlay.value = true
                    _journeyGroup.value=it
                }
            }
        }
    }
    fun refreshJourney(course: Course) {
        viewModelScope.launch {
            _journeyGroupInViewport.apply {
                this.value += getJourneyUseCase(course)
            }
        }
    }

    fun fetchNearByJourneyInMap(current: LatLng, distance: Int) {
        viewModelScope.launch {
            _journeyGroupInViewport.value += getNearByJourneyUseCase.byDistance(current, distance)
        }
    }

    fun fetchNearByJourneyInMap(current: LatLng, viewPort: Viewport) {
        viewModelScope.launch {
            _journeyGroupInViewport.value += getNearByJourneyUseCase.byViewport(current, viewPort)
                .sortedBy { it.code }
        }
    }

    fun fetchNearByJourneyInList(latLng: LatLng) {
        viewModelScope.launch {
            _journeyGroupInCenter.value = getNearByJourneyUseCase.byDistance(latLng,1500)
        }
    }

    fun refresh(){
        viewModelScope.launch {
            _journeyGroup.value = _journeyGroupInCenter.value
            _isRefreshOverlay.value = false
        }
    }

    fun hideJourneyWithoutItem(itemCode:Int){
        viewModelScope.launch {
            if(_journeyGroup.value.size==1 && _journeyGroup.value.first().code==itemCode)
                refresh()
            else
                _journeyGroup.value = _journeyGroup.value.filter { it.code == itemCode }
        }
    }
}