package com.wheretogo.data.datasource

import com.wheretogo.data.model.map.DataLatLng
import com.wheretogo.data.model.route.RemoteRoute

interface RouteRemoteDatasource {

    suspend fun getRouteInCourse(courseId: String): Result<RemoteRoute>

    suspend fun setRouteInCourse(remoteRoute: RemoteRoute): Result<Unit>

    suspend fun removeRouteInCourse(courseId: String): Result<Unit>

    suspend fun getRouteByNaver(waypoints: List<DataLatLng>): Result<RemoteRoute>
}