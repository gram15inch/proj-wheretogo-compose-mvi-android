package com.wheretogo.domain.usecase

import com.wheretogo.domain.repository.JourneyRepository
import com.wheretogo.domain.model.Course
import com.wheretogo.domain.model.Journey
import javax.inject.Inject

class GetJourneyUseCaseImpl @Inject constructor(private val journeyRepository: JourneyRepository) :
    GetJourneyUseCase {
    override suspend fun invoke(course: Course): Journey {
        return journeyRepository.getJourney(course)
    }
}