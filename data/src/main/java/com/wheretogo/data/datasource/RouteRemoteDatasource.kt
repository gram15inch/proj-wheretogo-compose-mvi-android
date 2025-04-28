package com.wheretogo.data.datasource

import com.wheretogo.data.model.map.DataLatLng
import com.wheretogo.data.model.route.RemoteRoute

interface RouteRemoteDatasource {

    suspend fun getRouteInCourse(courseId: String): RemoteRoute

    suspend fun setRouteInCourse(remoteRoute: RemoteRoute): Boolean

    suspend fun removeRouteInCourse(courseId: String): Boolean

    suspend fun getRouteByNaver(waypoints: List<DataLatLng>): RemoteRoute
}