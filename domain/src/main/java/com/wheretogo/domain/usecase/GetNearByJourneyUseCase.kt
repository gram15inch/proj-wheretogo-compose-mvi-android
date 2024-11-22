package com.wheretogo.domain.usecase

import com.wheretogo.domain.model.map.Journey
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.model.map.Viewport

interface GetNearByJourneyUseCase {
    suspend fun byDistance(current: LatLng, distance: Int): List<Journey>
    suspend fun byViewport(current: LatLng, viewport: Viewport): List<Journey>
}