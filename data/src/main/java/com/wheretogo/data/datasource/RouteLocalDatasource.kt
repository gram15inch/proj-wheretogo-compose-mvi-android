package com.wheretogo.data.datasource

import com.wheretogo.data.model.route.LocalRoute

interface RouteLocalDatasource {

    suspend fun getRouteInCourse(courseId: String): Result<LocalRoute>

    suspend fun setRouteInCourse(route: LocalRoute): Result<Unit>

    suspend fun removeRouteInCourse(courseId: String): Result<Unit>

}