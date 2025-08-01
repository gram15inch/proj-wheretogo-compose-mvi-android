package com.dhkim139.wheretogo.mock

import com.wheretogo.data.datasource.CourseRemoteDatasource
import com.wheretogo.data.model.course.RemoteCourse
import javax.inject.Inject

class MockCourseRemoteDatasourceImpl @Inject constructor() : CourseRemoteDatasource {
    private var newCourseGroup = mutableListOf<RemoteCourse>()
    override suspend fun getCourse(courseId: String): RemoteCourse? {
        return newCourseGroup.firstOrNull { it.courseId == courseId }
    }

    override suspend fun getCourseGroupByGeoHash(start: String, end: String): List<RemoteCourse> {
        return newCourseGroup.toList()
    }

    override suspend fun getCourseGroupByKeyword(keyword: String): List<RemoteCourse> {
        return newCourseGroup.toList()
    }

    override suspend fun setCourse(course: RemoteCourse): Boolean {
        return newCourseGroup.add(course)
    }

    override suspend fun removeCourse(courseId: String): Boolean {
        return newCourseGroup.removeIf { it.courseId == courseId }
    }
}