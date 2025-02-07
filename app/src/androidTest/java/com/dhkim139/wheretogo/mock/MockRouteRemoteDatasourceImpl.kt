package com.dhkim139.wheretogo.mock

import com.wheretogo.data.datasource.RouteRemoteDatasource
import com.wheretogo.data.model.route.RemoteRoute
import com.wheretogo.domain.model.map.LatLng
import javax.inject.Inject

class MockRouteRemoteDatasourceImpl @Inject constructor() : RouteRemoteDatasource {
    override suspend fun getRouteInCourse(courseId: String): RemoteRoute {
        return RemoteRoute(courseId)
    }

    override suspend fun setRouteInCourse(remoteRoute: RemoteRoute): Boolean {
        return true
    }

    override suspend fun removeRouteInCourse(courseId: String): Boolean {
        return true
    }

    override suspend fun getRouteByNaver(waypoints: List<LatLng>): RemoteRoute {
        return RemoteRoute()
    }
}