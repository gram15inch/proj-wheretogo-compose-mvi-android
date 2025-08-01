package com.wheretogo.data.datasource

import com.wheretogo.data.model.course.LocalSnapshot
import com.wheretogo.data.model.course.LocalCourse
import com.wheretogo.data.model.meta.LocalMetaGeoHash


interface CourseLocalDatasource {

    suspend fun getCourse(courseId: String): LocalCourse?

    suspend fun setCourse(courseGroup: List<LocalCourse>)

    suspend fun removeCourse(courseId: String)

    suspend fun getCourseGroupByGeoHash(geoHash: String): List<LocalCourse>

    suspend fun getMetaGeoHash(): List<LocalMetaGeoHash>

    suspend fun isExistMetaGeoHash(geoHash: String): Boolean

    suspend fun setMetaGeoHash(entity: LocalMetaGeoHash)

    suspend fun updateSnapshot(localSnapshot: LocalSnapshot)

    suspend fun appendIndex(localSnapshot: LocalSnapshot)

    suspend fun removeIndex(localSnapshot: LocalSnapshot)
}