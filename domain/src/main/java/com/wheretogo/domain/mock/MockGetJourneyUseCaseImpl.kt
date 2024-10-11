package com.wheretogo.domain.mock

import com.wheretogo.domain.repository.JourneyRepository
import com.wheretogo.domain.model.Course
import com.wheretogo.domain.model.Journey
import com.wheretogo.domain.usecase.GetJourneyUseCase
import javax.inject.Inject

class MockGetJourneyUseCaseImpl @Inject constructor() :
    GetJourneyUseCase {
    override suspend fun invoke(course: Course): Journey {
        return Journey.empty()
    }
}