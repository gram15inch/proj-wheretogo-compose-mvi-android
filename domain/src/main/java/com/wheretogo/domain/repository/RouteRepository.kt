package com.wheretogo.domain.repository


import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.model.map.Route

interface RouteRepository {

    suspend fun getRouteInCourse(courseId: String): Result<Route>

    suspend fun setRouteInCourse(route: Route): Result<Unit>

    suspend fun removeRouteInCourse(courseId: String): Result<Unit>

    suspend fun createRoute(waypoints: List<LatLng>): Result<Route>

}