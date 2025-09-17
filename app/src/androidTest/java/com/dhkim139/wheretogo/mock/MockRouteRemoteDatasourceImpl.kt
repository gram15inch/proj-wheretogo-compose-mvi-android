package com.dhkim139.wheretogo.mock

import com.wheretogo.data.datasource.RouteRemoteDatasource
import com.wheretogo.data.feature.dataErrorCatching
import com.wheretogo.data.model.map.DataLatLng
import com.wheretogo.data.model.route.RemoteRoute
import javax.inject.Inject

class MockRouteRemoteDatasourceImpl @Inject constructor() : RouteRemoteDatasource {
    override suspend fun getRouteInCourse(courseId: String): Result<RemoteRoute> {
        return dataErrorCatching { RemoteRoute(courseId) }
    }

    override suspend fun setRouteInCourse(remoteRoute: RemoteRoute): Result<Unit> {
        return dataErrorCatching { }
    }

    override suspend fun removeRouteInCourse(courseId: String): Result<Unit> {
        return dataErrorCatching { }
    }

    override suspend fun getRouteByNaver(waypoints: List<DataLatLng>): Result<RemoteRoute> {
        return dataErrorCatching { RemoteRoute() }
    }
}