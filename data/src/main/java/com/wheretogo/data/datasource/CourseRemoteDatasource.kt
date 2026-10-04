package com.wheretogo.data.datasource

import com.wheretogo.data.ApiResult
import com.wheretogo.data.model.course.Page

interface CourseRemoteDatasource {

    suspend fun fetchPage(updateAt: Long): ApiResult<Page>

    suspend fun removeCourse(courseId: String): ApiResult<Unit>
}