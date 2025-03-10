package com.wheretogo.data.repositoryimpl


import com.wheretogo.data.datasource.RouteRemoteDatasource
import com.wheretogo.data.toRoute
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.model.map.Route
import com.wheretogo.domain.repository.RouteRepository
import javax.inject.Inject

class RouteRepositoryImpl @Inject constructor(
    private val remoteDatasource: RouteRemoteDatasource
) : RouteRepository {

    override suspend fun getRouteInCourse(courseId: String): Result<Route> {
        return runCatching {
            remoteDatasource.getRouteInCourse(courseId).toRoute()
        }
    }

    override suspend fun setRouteInCourse(route: Route): Result<Unit> {
        return runCatching {
            remoteDatasource.setRouteInCourse(route.toRoute())
        }
    }

    override suspend fun removeRouteInCourse(courseId: String): Result<Unit> {
        return runCatching {
            remoteDatasource.removeRouteInCourse(courseId)
        }
    }

    override suspend fun createRoute(waypoints: List<LatLng>): Result<Route> {
        return runCatching {
            remoteDatasource.getRouteByNaver(waypoints).toRoute()
        }
    }
}