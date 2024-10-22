package com.wheretogo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheretogo.domain.model.Course
import com.wheretogo.domain.model.Journey
import com.wheretogo.domain.model.LatLng
import com.wheretogo.domain.model.Viewport
import com.wheretogo.domain.usecase.GetJourneyUseCase
import com.wheretogo.domain.usecase.GetNearByJourneyUseCase
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
    private val _journeyGroupInMap = MutableStateFlow<Set<Journey>>(emptySet())
    private val _journeyGroupInList = MutableStateFlow<List<Journey>>(emptyList())
    val journeyGroupInMap: StateFlow<Set<Journey>> get() = _journeyGroupInMap
    val journeyGroupInList: StateFlow<List<Journey>> get() = _journeyGroupInList
    fun refreshJourney(course: Course) {
        viewModelScope.launch {
            _journeyGroupInMap.apply {
                this.value += getJourneyUseCase(course)
            }
        }
    }

    fun fetchNearByJourneyInMap(current: LatLng, distance: Int) {
        viewModelScope.launch {
            _journeyGroupInMap.value += getNearByJourneyUseCase.byDistance(current, distance)
        }
    }

    fun fetchNearByJourneyInMap(current: LatLng, viewPort: Viewport) {
        viewModelScope.launch {
            _journeyGroupInMap.value += getNearByJourneyUseCase.byViewport(current, viewPort)
                .sortedBy { it.code }
        }
    }

    fun fetchNearByJourneyInList(latLng: LatLng) {
        viewModelScope.launch {
            _journeyGroupInList.value = getNearByJourneyUseCase.byDistance(latLng,1000)
        }
    }
}