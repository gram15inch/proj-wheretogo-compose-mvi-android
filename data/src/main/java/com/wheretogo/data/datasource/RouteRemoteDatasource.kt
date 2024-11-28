package com.wheretogo.data.datasource

import com.wheretogo.data.model.route.RemoteRoute

interface RouteRemoteDatasource {

    suspend fun getRouteInCourse(courseId: String): RemoteRoute

    suspend fun setRouteInCourse(courseId: String, remoteRoute: RemoteRoute): Boolean

    suspend fun removeRouteInCourse(courseId: String): Boolean

    fun getRouteId(courseId: String): String
}