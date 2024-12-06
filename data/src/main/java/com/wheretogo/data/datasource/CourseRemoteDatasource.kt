package com.wheretogo.data.datasource

import com.wheretogo.data.model.course.RemoteCourse

interface CourseRemoteDatasource {

    suspend fun getCourse(courseId: String): RemoteCourse?

    suspend fun getCourseGroupByGeoHash(start: String, end: String): List<RemoteCourse>

    suspend fun setCourse(course: RemoteCourse): Boolean

    suspend fun removeCourse(courseId: String): Boolean
}