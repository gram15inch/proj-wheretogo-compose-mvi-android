package com.dhkim139.wheretogo.mock

import com.wheretogo.data.datasource.CourseRemoteDatasource
import com.wheretogo.data.feature.dataErrorCatching
import com.wheretogo.data.model.course.RemoteCourse
import javax.inject.Inject

class MockCourseRemoteDatasourceImpl @Inject constructor() : CourseRemoteDatasource {
    private var newCourseGroup = mutableListOf<RemoteCourse>()
    override suspend fun getCourse(courseId: String): Result<RemoteCourse> {
        return dataErrorCatching { newCourseGroup.firstOrNull { it.courseId == courseId } }
    }

    override suspend fun getCourseGroupByGeoHash(
        start: String,
        end: String
    ): Result<List<RemoteCourse>> {
        return dataErrorCatching { newCourseGroup.toList() }
    }

    override suspend fun getCourseGroupByKeyword(keyword: String): Result<List<RemoteCourse>> {
        return dataErrorCatching { newCourseGroup.toList() }
    }

    override suspend fun setCourse(course: RemoteCourse): Result<Unit> {
        return dataErrorCatching { newCourseGroup.add(course) }
            .mapCatching { Unit }
    }

    override suspend fun removeCourse(courseId: String): Result<Unit> {
        return dataErrorCatching { newCourseGroup.removeIf { it.courseId == courseId } }
            .mapCatching { Unit }
    }
}