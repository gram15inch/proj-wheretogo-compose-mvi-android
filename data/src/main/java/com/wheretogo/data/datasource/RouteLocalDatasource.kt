package com.wheretogo.data.datasource

import com.wheretogo.data.model.route.RoutePathEntity
import com.wheretogo.domain.model.course.RoutePath

interface RouteLocalDatasource {

    suspend fun getRoute(routeId: String): RoutePath?

    suspend fun setRoute(entity: RoutePathEntity)

}