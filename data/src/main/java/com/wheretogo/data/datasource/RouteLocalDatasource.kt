package com.wheretogo.data.datasource

import com.wheretogo.data.model.route.LocalRoute

interface RouteLocalDatasource {

    suspend fun getRouteInCourse(courseId: String): LocalRoute?

    suspend fun setRouteInCourse(route: LocalRoute)

    suspend fun removeRouteInCourse(courseId: String)

}