package com.wheretogo.domain.usecase.util

import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.route.Route

interface CreateRouteUseCase {
    suspend operator fun invoke(waypoints: List<LatLng>): Result<Route>
}