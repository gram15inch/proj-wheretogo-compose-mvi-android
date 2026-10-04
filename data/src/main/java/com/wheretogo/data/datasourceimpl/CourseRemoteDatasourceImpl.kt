package com.wheretogo.data.datasourceimpl

import com.wheretogo.data.ApiResult
import com.wheretogo.data.apiCall
import com.wheretogo.data.model.course.CourseDto
import com.wheretogo.data.datasourceimpl.service.CourseManageApi
import com.wheretogo.data.datasourceimpl.service.CourseSyncApi
import com.wheretogo.data.datasource.CourseRemoteDatasource
import com.wheretogo.data.map
import com.wheretogo.data.model.course.Page
import com.wheretogo.domain.model.course.LoadingConfig.SYNC_PAGE_LIMIT
import javax.inject.Inject

class CourseRemoteDatasourceImpl @Inject constructor(
    private val syncApi: CourseSyncApi,
    private val manageApi: CourseManageApi,
) : CourseRemoteDatasource {

    override suspend fun fetchPage(updateAt: Long): ApiResult<Page> =
        apiCall {
            syncApi.sync(cursor = updateAt, limit = SYNC_PAGE_LIMIT)
        }.map {
            val upserts = mutableListOf<CourseDto>()
            val deletedIds = mutableListOf<String>()
            it.courses.orEmpty().forEach { dto ->
                when {
                    dto.deleted == true -> dto.id?.let(deletedIds::add)
                    else -> dto.let(upserts::add)
                }
            }
            Page(
                upserts = upserts,
                deletedIds = deletedIds,
                cursor = it.cursor,
                hasMore = it.hasMore == true,
                resync = it.resync == true,
            )
        }


    override suspend fun removeCourse(courseId: String): ApiResult<Unit> =
        apiCall { manageApi.delete(courseId) }
}