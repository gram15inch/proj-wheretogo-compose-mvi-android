package com.wheretogo.domain.usecaseimpl

import com.wheretogo.domain.repository.JourneyRepository
import com.wheretogo.domain.model.Course
import com.wheretogo.domain.model.Journey
import com.wheretogo.domain.usecase.GetJourneyUseCase
import javax.inject.Inject

class GetJourneyUseCaseImpl @Inject constructor(private val journeyRepository: JourneyRepository) :
    GetJourneyUseCase {
    override suspend fun invoke(course: Course): Journey {
        return journeyRepository.getJourney(course)
    }
}