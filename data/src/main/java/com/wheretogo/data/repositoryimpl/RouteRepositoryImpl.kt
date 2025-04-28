package com.wheretogo.data.repositoryimpl


import com.wheretogo.data.datasource.RouteLocalDatasource
import com.wheretogo.data.datasource.RouteRemoteDatasource
import com.wheretogo.data.toDataLatLngGroup
import com.wheretogo.data.toLocalRoute
import com.wheretogo.data.toRemoteRoute
import com.wheretogo.data.toRoute
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.model.map.Route
import com.wheretogo.domain.repository.RouteRepository
import javax.inject.Inject

class RouteRepositoryImpl @Inject constructor(
    private val routeRemoteDatasource: RouteRemoteDatasource,
    private val routeLocalDatasource: RouteLocalDatasource
) : RouteRepository {
    override suspend fun getRouteInCourse(courseId: String): Result<Route> {
        return runCatching {
            return routeLocalDatasource.getRouteInCourse(courseId)?.let {
                Result.success(it.toRoute())
            }?: run{
                val route = routeRemoteDatasource.getRouteInCourse(courseId).toRoute()
                routeLocalDatasource.setRouteInCourse(route.toLocalRoute())
               Result.success(route)
            }
        }
    }

    override suspend fun setRouteInCourse(route: Route): Result<Unit> {
        return runCatching {
            routeRemoteDatasource.setRouteInCourse(route.toRemoteRoute())
            routeLocalDatasource.setRouteInCourse(route.toLocalRoute())
        }
    }

    override suspend fun removeRouteInCourse(courseId: String): Result<Unit> {
        return runCatching {
            routeRemoteDatasource.removeRouteInCourse(courseId)
            routeLocalDatasource.removeRouteInCourse(courseId)
        }
    }

    override suspend fun createRoute(waypoints: List<LatLng>): Result<Route> {
        return runCatching {
            routeRemoteDatasource.getRouteByNaver(waypoints.toDataLatLngGroup()).toRoute()
        }
    }
}