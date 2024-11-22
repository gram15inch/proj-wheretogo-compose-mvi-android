package com.wheretogo.domain.mock

import com.wheretogo.domain.model.map.Course
import com.wheretogo.domain.model.map.Journey
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.model.map.Viewport
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