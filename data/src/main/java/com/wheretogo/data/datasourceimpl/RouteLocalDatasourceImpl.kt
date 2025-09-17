package com.wheretogo.data.datasourceimpl

import com.wheretogo.data.datasource.RouteLocalDatasource
import com.wheretogo.data.datasourceimpl.database.RouteDatabase
import com.wheretogo.data.feature.dataErrorCatching
import com.wheretogo.data.model.route.LocalRoute
import javax.inject.Inject

class RouteLocalDatasourceImpl @Inject constructor(
    private val routeDatabase: RouteDatabase
) : RouteLocalDatasource {
    private val routeDao by lazy { routeDatabase.routeDao() }
    override suspend fun getRouteInCourse(courseId: String): Result<LocalRoute> {
        return dataErrorCatching { routeDao.select(courseId) }
    }

    override suspend fun setRouteInCourse(route: LocalRoute): Result<Unit> {
        return dataErrorCatching { routeDao.insert(route) }
    }

    override suspend fun removeRouteInCourse(courseId: String): Result<Unit> {
        return dataErrorCatching { routeDao.delete(courseId) }
    }
}