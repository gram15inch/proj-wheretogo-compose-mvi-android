package com.wheretogo.domain.repository


import com.wheretogo.domain.model.course.RoutePath

interface RouteRepository {

    suspend fun getRoutePath(routeId: String): Result<RoutePath?>

}