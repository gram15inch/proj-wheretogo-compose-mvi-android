package com.wheretogo.data.datasource

import com.wheretogo.data.model.route.RemoteRoute
import com.wheretogo.domain.model.map.LatLng

interface RouteRemoteDatasource {

    suspend fun getRouteInCourse(courseId: String): RemoteRoute

    suspend fun setRouteInCourse(remoteRoute: RemoteRoute): Boolean

    suspend fun removeRouteInCourse(courseId: String): Boolean

    suspend fun getRouteByNaver(waypoints: List<LatLng>): RemoteRoute

    suspend fun getAddress(latlng: LatLng): String
}