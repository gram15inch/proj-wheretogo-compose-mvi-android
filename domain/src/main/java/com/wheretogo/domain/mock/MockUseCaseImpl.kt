package com.wheretogo.domain.mock

import com.wheretogo.domain.model.Course
import com.wheretogo.domain.model.Journey
import com.wheretogo.domain.model.LatLng
import com.wheretogo.domain.model.Viewport
import com.wheretogo.domain.usecase.FetchJourneyWithoutPointsUseCase
import com.wheretogo.domain.usecase.GetJourneyUseCase
import com.wheretogo.domain.usecase.GetNearByJourneyUseCase
import javax.inject.Inject

class MockUseCaseImpl @Inject constructor() :
    GetJourneyUseCase,
    GetNearByJourneyUseCase,
    FetchJourneyWithoutPointsUseCase {
    override suspend fun invoke(course: Course): Journey {
        return Journey()
    }

    override suspend fun byDistance(current: LatLng, distance: Int): List<Journey> {
        return emptyList()
    }

    override suspend fun byViewport(current: LatLng, viewport: Viewport): List<Journey> {
        return emptyList()
    }

    override suspend fun invoke() {}
}