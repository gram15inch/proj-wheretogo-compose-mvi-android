package com.wheretogo.domain.repository


import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.model.map.Route

interface RouteRepository {

    suspend fun getRouteInCourse(courseId: String): Route

    suspend fun setRouteInCourse(route: Route): Boolean

    suspend fun removeRouteInCourse(courseId: String): Boolean

    suspend fun createRoute(waypoints: List<LatLng>): Route

}