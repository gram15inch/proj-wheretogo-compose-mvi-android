package com.wheretogo.data.datasourceimpl

import com.wheretogo.data.datasource.RouteRemoteDatasource
import com.wheretogo.data.datasourceimpl.service.RouteApi
import com.wheretogo.data.model.route.RoutePathResponseDto
import javax.inject.Inject

class RouteRemoteDatasourceImpl @Inject constructor(
    private val routePathApi: RouteApi
) : RouteRemoteDatasource {

    override suspend fun getRoute(routeId: String): RoutePathResponseDto? {
        return routePathApi.path(routeId).body()
    }
}