package com.wheretogo.domain.repository

import com.wheretogo.domain.model.map.Course
import com.wheretogo.domain.model.map.Snapshot

interface CourseRepository {
    suspend fun getCourse(courseId: String): Result<Course>

    suspend fun getCourseGroupByGeoHash(geoHash: String): Result<List<Course>>

    suspend fun getCourseGroupByKeyword(keyword: String): Result<List<Course>>

    suspend fun setCourse(
        course: Course,
        keyword: List<String> = emptyList()
    ): Result<Unit>

    suspend fun removeCourse(courseId: String): Result<Unit>

    suspend fun getSnapshot(courseId: String): Snapshot

    suspend fun updateSnapshot(snapshot: Snapshot): Result<Unit>

    suspend fun appendIndex(snapshot: Snapshot): Result<Unit>

    suspend fun removeIndex(snapshot: Snapshot): Result<Unit>
}