package com.wheretogo.data.datasource

import com.wheretogo.data.model.course.RemoteCourse

interface CourseRemoteDatasource {

    suspend fun getCourse(courseId: String): Result<RemoteCourse>

    suspend fun getCourseGroupByKeyword(keyword: String): Result<List<RemoteCourse>>

    suspend fun getCourseGroupByUpdateAt(updateAt: Long): Result<List<RemoteCourse>>

    suspend fun setCourse(course: RemoteCourse): Result<Unit>

    suspend fun removeCourse(courseId: String): Result<Unit>
}