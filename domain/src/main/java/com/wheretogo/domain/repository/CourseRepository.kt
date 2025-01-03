package com.wheretogo.domain.repository

import com.wheretogo.domain.model.map.CheckPoint
import com.wheretogo.domain.model.map.Course

interface CourseRepository {
    suspend fun getCourse(courseId: String): Course?

    suspend fun getCourseGroupByGeoHash(geoHash: String): List<Course>

    suspend fun setCourse(
        course: Course,
        checkPoints: List<CheckPoint> = emptyList()
    )
}