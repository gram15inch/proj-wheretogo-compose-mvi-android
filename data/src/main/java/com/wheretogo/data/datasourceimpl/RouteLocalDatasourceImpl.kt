package com.wheretogo.data.datasourceimpl

import com.wheretogo.data.datasource.RouteLocalDatasource
import com.wheretogo.data.datasourceimpl.database.RouteDatabase
import com.wheretogo.data.model.route.LocalRoute
import javax.inject.Inject

class RouteLocalDatasourceImpl @Inject constructor(
    private val routeDatabase: RouteDatabase
) : RouteLocalDatasource {
    private val routeDao by lazy { routeDatabase.routeDao() }
    override suspend fun getRouteInCourse(courseId: String): LocalRoute? {
        return routeDao.select(courseId)
    }

    override suspend fun setRouteInCourse(route: LocalRoute) {
        routeDao.insert(route)
    }

    override suspend fun removeRouteInCourse(courseId: String) {
        routeDao.delete(courseId)
    }
}