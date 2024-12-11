package com.wheretogo.data.datasource

import com.wheretogo.data.model.course.DataMetaCheckPoint
import com.wheretogo.data.model.course.LocalCourse
import com.wheretogo.data.model.meta.LocalMetaGeoHash


interface CourseLocalDatasource {

    suspend fun getCourse(courseId: String): LocalCourse?

    suspend fun setCourse(course: LocalCourse)

    suspend fun removeCourse(courseId: String)

    suspend fun getCourseGroupByGeoHash(geoHash: String): List<LocalCourse>

    suspend fun getMetaGeoHash(): List<LocalMetaGeoHash>

    suspend fun isExistMetaGeoHash(geoHash: String): Boolean

    suspend fun setMetaGeoHash(entity: LocalMetaGeoHash)

    suspend fun updateMetaCheckPoint(courseId: String, dataMetaCheckPoint: DataMetaCheckPoint)
}