package com.wheretogo.domain.usecase

import com.wheretogo.domain.model.Journey
import com.wheretogo.domain.model.LatLng
import com.wheretogo.domain.model.Viewport

interface GetNearByJourneyUseCase {
    suspend fun byDistance(current: LatLng, distance: Int): List<Journey>
    suspend fun byViewport(current: LatLng, viewport: Viewport): List<Journey>
}