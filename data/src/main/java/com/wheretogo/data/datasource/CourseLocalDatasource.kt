package com.wheretogo.data.datasource

import com.wheretogo.data.model.course.LocalCourse
import com.wheretogo.data.model.course.LocalSnapshot
import com.wheretogo.data.model.meta.LocalMetaGeoHash


interface CourseLocalDatasource {

    suspend fun getCourse(courseId: String): Result<LocalCourse?>

    suspend fun setCourse(courseGroup: List<LocalCourse>): Result<Unit>

    suspend fun removeCourse(courseId: String): Result<Unit>

    suspend fun getCourseGroupByGeoHash(geoHash: String): Result<List<LocalCourse>>

    suspend fun getMetaGeoHash(): Result<List<LocalMetaGeoHash>>

    suspend fun isExistMetaGeoHash(geoHash: String): Result<Boolean>

    suspend fun setMetaGeoHash(entity: LocalMetaGeoHash): Result<Unit>

    suspend fun updateSnapshot(localSnapshot: LocalSnapshot): Result<Unit>

    suspend fun appendIndex(localSnapshot: LocalSnapshot): Result<Unit>

    suspend fun removeIndex(localSnapshot: LocalSnapshot): Result<Unit>

    suspend fun clear(): Result<Unit>
}