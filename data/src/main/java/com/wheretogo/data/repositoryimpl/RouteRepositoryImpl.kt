package com.wheretogo.data.repositoryimpl


import com.wheretogo.data.DataError
import com.wheretogo.data.datasourceimpl.RouteLocalDatasourceImpl
import com.wheretogo.data.datasourceimpl.RouteRemoteDatasourceImpl
import com.wheretogo.data.model.route.toEntity
import com.wheretogo.domain.model.course.RoutePath
import com.wheretogo.domain.repository.RouteRepository
import javax.inject.Inject

class RouteRepositoryImpl @Inject constructor(
    private val routeRemoteDatasource: RouteRemoteDatasourceImpl,
    private val routeLocalDatasource: RouteLocalDatasourceImpl
) : RouteRepository {

     override suspend fun getRoutePath(routeId:String): Result<RoutePath?>{
        return runCatching {
            val route= routeLocalDatasource.getRoute(routeId)?:routeRemoteDatasource.getRoute(routeId)?.run {
                val entity= toEntity(routeId, System.currentTimeMillis())?:return@runCatching null
                routeLocalDatasource.setRoute(entity)
                routeLocalDatasource.getRoute(routeId)?:throw DataError.InternalError("")
            }?:return@runCatching null
            route
        }
    }

}