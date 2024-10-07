package com.wheretogo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.wheretogo.domain.model.Course
import com.wheretogo.domain.model.Journey
import com.wheretogo.domain.repository.JourneyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DriveViewModel @Inject constructor(private val journeyRepository: JourneyRepository) :ViewModel() {

    suspend fun getJourney(course: Course): Journey {
        return journeyRepository.getJourney(course)
    }
}