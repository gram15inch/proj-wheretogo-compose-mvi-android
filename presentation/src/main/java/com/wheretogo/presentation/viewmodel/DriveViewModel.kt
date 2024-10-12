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
    private val _journeyGroup = MutableStateFlow<List<Journey>>(emptyList())
    val journeyGroup: StateFlow<List<Journey>> get() = _journeyGroup

    fun refreshJourney(course: Course) {
        viewModelScope.launch {
            _journeyGroup.apply {
                this.value += getJourneyUseCase(course)
            }
        }
    }

    fun fetchNearByJourney(current: LatLng, distance: Int) {
        viewModelScope.launch {
            _journeyGroup.value += getNearByJourneyUseCase(current, distance)
        }
    }

    fun fetchNearByJourney(current: LatLng, viewPort: Viewport) {
        viewModelScope.launch {
        _journeyGroup.value = getNearByJourneyUseCase.by(current, viewPort).sortedBy { it.code }
        }
    }
}