package com.wheretogo.domain.usecase.map

import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.model.map.Route

interface CreateRouteUseCase {
    suspend operator fun invoke(waypoints: List<LatLng>): Route?
}