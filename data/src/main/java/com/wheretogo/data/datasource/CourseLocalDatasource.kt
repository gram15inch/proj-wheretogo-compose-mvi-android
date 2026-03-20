package com.wheretogo.data.datasource

import com.wheretogo.data.model.course.LocalCourse
import com.wheretogo.data.model.course.LocalSnapshot


interface CourseLocalDatasource {

    suspend fun getCourse(courseId: String): Result<LocalCourse?>

    suspend fun setCourse(courseGroup: List<LocalCourse>): Result<Unit>

    suspend fun removeCourse(courseId: String): Result<Unit>

    suspend fun getCourseGroupByGeoHash(geoHash: String): Result<List<LocalCourse>>

    suspend fun updateSnapshot(localSnapshot: LocalSnapshot): Result<Unit>

    suspend fun appendIndex(localSnapshot: LocalSnapshot): Result<Unit>

    suspend fun removeIndex(localSnapshot: LocalSnapshot): Result<Unit>

    suspend fun clear(): Result<Unit>

    suspend fun getLatestUpdate(): Result<Long>

    suspend fun setLatestUpdate(updateAt: Long): Result<Unit>

    suspend fun getCourseByIsHide(isHide: Boolean): Result<List<LocalCourse>>
}