package com.wheretogo.data.datasource

import com.wheretogo.data.model.route.RoutePathResponseDto

interface RouteRemoteDatasource {
    suspend fun getRoute(routeId: String): RoutePathResponseDto?
}