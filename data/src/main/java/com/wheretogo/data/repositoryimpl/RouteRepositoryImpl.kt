package com.wheretogo.data.repositoryimpl


import com.wheretogo.data.datasource.RouteLocalDatasource
import com.wheretogo.data.datasource.RouteRemoteDatasource
import com.wheretogo.data.feature.catchNotFound
import com.wheretogo.data.feature.mapDomainError
import com.wheretogo.data.feature.mapSuccess
import com.wheretogo.data.toDataLatLngGroup
import com.wheretogo.data.toLocalRoute
import com.wheretogo.data.toRemoteRoute
import com.wheretogo.data.toRoute
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.route.Route
import com.wheretogo.domain.repository.RouteRepository
import javax.inject.Inject

class RouteRepositoryImpl @Inject constructor(
    private val routeRemoteDatasource: RouteRemoteDatasource,
    private val routeLocalDatasource: RouteLocalDatasource
) : RouteRepository {
    override suspend fun getRouteInCourse(courseId: String): Result<Route> {
        return routeLocalDatasource.getRouteInCourse(courseId)
            .catchNotFound{
                routeRemoteDatasource.getRouteInCourse(courseId).mapSuccess {
                    val local = it.toLocalRoute()
                    routeLocalDatasource.setRouteInCourse(local)
                        .mapCatching { local }
                }
            }.mapCatching { it.toRoute() }.mapDomainError()
    }

    override suspend fun setRouteInCourse(route: Route): Result<Unit> {
        val remote = route.toRemoteRoute()
        return routeRemoteDatasource.setRouteInCourse(route.toRemoteRoute()).mapSuccess {
            routeLocalDatasource.setRouteInCourse(remote.toLocalRoute())
        }.mapDomainError()
    }

    override suspend fun removeRouteInCourse(courseId: String): Result<Unit> {
        return routeRemoteDatasource.removeRouteInCourse(courseId).mapSuccess {
            routeLocalDatasource.removeRouteInCourse(courseId)
        }.mapDomainError()
    }

    override suspend fun createRoute(waypoints: List<LatLng>): Result<Route> {
        return routeRemoteDatasource.getRouteByNaver(waypoints.toDataLatLngGroup())
            .mapCatching { it.toRoute() }
            .mapDomainError()
    }
}