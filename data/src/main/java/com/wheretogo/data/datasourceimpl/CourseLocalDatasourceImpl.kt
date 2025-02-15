package com.wheretogo.data.datasourceimpl

import com.wheretogo.data.datasource.CourseLocalDatasource
import com.wheretogo.data.datasourceimpl.database.CourseDatabase
import com.wheretogo.data.model.course.DataMetaCheckPoint
import com.wheretogo.data.model.course.LocalCourse
import com.wheretogo.data.model.meta.LocalMetaGeoHash
import javax.inject.Inject

class CourseLocalDatasourceImpl @Inject constructor(
    private val courseDatabase: CourseDatabase
) : CourseLocalDatasource {
    private val courseDao by lazy { courseDatabase.courseDao() }
    override suspend fun getCourse(courseId: String): LocalCourse? {
        return courseDao.select(courseId)
    }

    override suspend fun setCourse(course: LocalCourse) {
        courseDao.insert(course)
    }

    override suspend fun removeCourse(courseId: String) {
        return courseDao.delete(courseId)
    }

    override suspend fun getCourseGroupByGeoHash(geoHash: String): List<LocalCourse> {
        return courseDao.selectByGeoHash(geoHash)
    }

    override suspend fun getMetaGeoHash(): List<LocalMetaGeoHash> {
        return courseDao.getMetaGeoHashGroup()
    }

    override suspend fun isExistMetaGeoHash(geoHash: String): Boolean {
        return courseDao.getMetaGeoHash(geoHash) != null
    }

    override suspend fun setMetaGeoHash(entity: LocalMetaGeoHash) {
        courseDao.setMetaGeoHash(entity)
    }

    override suspend fun updateMetaCheckPoint(
        courseId: String,
        dataMetaCheckPoint: DataMetaCheckPoint
    ) {
        courseDao.updateMetaCheckPoint(courseId, dataMetaCheckPoint)
    }

}