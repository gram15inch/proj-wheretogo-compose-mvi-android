package com.wheretogo.domain.repository

import com.wheretogo.domain.model.map.CheckPoint
import com.wheretogo.domain.model.map.Course
import com.wheretogo.domain.model.map.MetaCheckPoint

interface CourseRepository {
    suspend fun getCourse(courseId: String): Result<Course>

    suspend fun getCourseGroupByGeoHash(geoHash: String): Result<List<Course>>

    suspend fun setCourse(
        course: Course,
        checkPoints: List<CheckPoint> = emptyList()
    ): Result<Unit>

    suspend fun removeCourse(courseId: String): Result<Unit>

    suspend fun updateMetaCheckpoint(
        courseId: String,
        metaCheckPoint: MetaCheckPoint
    ): Result<Unit>
}