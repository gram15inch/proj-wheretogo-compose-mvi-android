package com.wheretogo.domain.mock

import com.wheretogo.domain.model.Journey
import com.wheretogo.domain.model.LatLng
import com.wheretogo.domain.usecase.GetNearByJourneyUseCase
import javax.inject.Inject

class MockGetNearByJourneyUseCaseImpl @Inject constructor() :
    GetNearByJourneyUseCase {
    override suspend fun invoke(current: LatLng, distance: Int): List<Journey> {
        return emptyList()
    }
}