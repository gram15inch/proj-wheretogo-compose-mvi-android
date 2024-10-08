package com.wheretogo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.wheretogo.domain.model.Course
import com.wheretogo.domain.model.Journey
import com.wheretogo.domain.usecase.GetJourneyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class DriveViewModel @Inject constructor(private val journeyUseCase: GetJourneyUseCase) :ViewModel() {
    private val _journeyGroup = MutableStateFlow<List<Journey>>(emptyList())
    val journeyGroup :StateFlow<List<Journey>> = _journeyGroup

    suspend fun refreshJourney(course: Course) {
        _journeyGroup.apply {
            this.value += journeyUseCase(course)
        }

    }
}