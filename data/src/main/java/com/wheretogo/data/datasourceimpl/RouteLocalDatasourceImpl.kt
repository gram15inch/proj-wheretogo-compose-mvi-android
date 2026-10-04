package com.wheretogo.data.datasourceimpl

import androidx.collection.LruCache
import com.wheretogo.data.datasource.RouteLocalDatasource
import com.wheretogo.data.datasourceimpl.database.CourseDatabase
import com.wheretogo.data.model.route.RoutePathEntity
import com.wheretogo.data.model.route.toDomain
import com.wheretogo.domain.model.course.LoadingConfig.DECODED_CACHE_ENTRIES
import com.wheretogo.domain.model.course.RoutePath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RouteLocalDatasourceImpl @Inject constructor(
    database: CourseDatabase
) : RouteLocalDatasource {
    private val routeDao by lazy { database.routePathDao() }

    private val decoded = LruCache<String, RoutePath>(DECODED_CACHE_ENTRIES)

    override suspend fun getRoute(routeId:String): RoutePath?{
        return withContext(Dispatchers.Default){
            val path = decoded[routeId]
                ?: routeDao.select(routeId)
                    ?.toDomain()?.also { decoded.put(routeId, it) }
            path
        }
    }

    override suspend fun setRoute(entity: RoutePathEntity){
        routeDao.upsert(entity)
    }

}