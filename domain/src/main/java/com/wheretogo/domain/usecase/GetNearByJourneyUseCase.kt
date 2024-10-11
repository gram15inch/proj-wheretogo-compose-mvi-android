package com.wheretogo.domain.usecase

import com.wheretogo.domain.model.Journey
import com.wheretogo.domain.model.LatLng

interface GetNearByJourneyUseCase {
    suspend operator fun invoke(current: LatLng, distance:Int): List<Journey>
}