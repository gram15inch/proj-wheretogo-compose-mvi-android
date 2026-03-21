package com.wheretogo.domain.repository

import com.wheretogo.domain.model.course.Course
import com.wheretogo.domain.model.course.CourseAddRequest

interface CourseRepository {
    suspend fun getCourse(courseId: String): Result<Course>

    suspend fun getCourseGroupByGeoHash(geoHash: String): Result<List<Course>>

    suspend fun getCourseGroupByKeyword(keyword: String): Result<List<Course>>

    suspend fun addCourse(request: CourseAddRequest): Result<Course>

    suspend fun removeCourse(courseId: String): Result<Unit>

    suspend fun clearExpired():  Result<Int>

    suspend fun clearCache(): Result<Unit>
}