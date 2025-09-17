package com.wheretogo.domain.repository

import com.wheretogo.domain.model.course.Course
import com.wheretogo.domain.model.course.CourseAddRequest
import com.wheretogo.domain.model.util.Snapshot

interface CourseRepository {
    suspend fun getCourse(courseId: String): Result<Course>

    suspend fun getCourseGroupByGeoHash(geoHash: String): Result<List<Course>>

    suspend fun getCourseGroupByKeyword(keyword: String): Result<List<Course>>

    suspend fun addCourse(request: CourseAddRequest): Result<Course>

    suspend fun removeCourse(courseId: String): Result<Unit>

    suspend fun getSnapshot(courseId: String): Result<Snapshot>

    suspend fun updateSnapshot(snapshot: Snapshot): Result<Unit>

    suspend fun appendIndexBySnapshot(refId: String, index: String): Result<Unit>

    suspend fun removeIndexBySnapshot(refId: String, index: String): Result<Unit>

    suspend fun clearCache(): Result<Unit>
}