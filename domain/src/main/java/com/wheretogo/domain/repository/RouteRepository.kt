package com.wheretogo.domain.repository


import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.route.Route

interface RouteRepository {

    suspend fun getRouteInCourse(courseId: String): Result<Route>

    suspend fun setRouteInCourse(route: Route): Result<Unit>

    suspend fun removeRouteInCourse(courseId: String): Result<Unit>

    suspend fun createRoute(waypoints: List<LatLng>): Result<Route>

}