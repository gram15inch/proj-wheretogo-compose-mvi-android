package com.wheretogo.data.datasourceimpl

import com.wheretogo.data.datasource.CourseLocalDatasource
import com.wheretogo.data.datasourceimpl.database.CourseDatabase
import com.wheretogo.data.model.course.LocalSnapshot
import com.wheretogo.data.model.course.LocalCourse
import com.wheretogo.data.model.meta.LocalMetaGeoHash
import com.wheretogo.domain.COURSE_UPDATE_TIME
import javax.inject.Inject

class CourseLocalDatasourceImpl @Inject constructor(
    private val courseDatabase: CourseDatabase
) : CourseLocalDatasource {
    private val courseDao by lazy { courseDatabase.courseDao() }
    override suspend fun getCourse(courseId: String): LocalCourse? {
        return courseDao.select(courseId)
    }

    override suspend fun setCourse(courseGroup: List<LocalCourse>) {
        courseDao.insert(courseGroup)
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
        val metaGeoHash = courseDao.getMetaGeoHash(geoHash)
        return metaGeoHash != null
                && (System.currentTimeMillis() - metaGeoHash.timestamp) < COURSE_UPDATE_TIME
    }

    override suspend fun setMetaGeoHash(entity: LocalMetaGeoHash) {
        courseDao.setMetaGeoHash(entity)
    }

    override suspend fun updateSnapshot(
        localSnapshot: LocalSnapshot
    ) {
        courseDao.updateSnapshot(
            courseId = localSnapshot.refId,
            localSnapshot = localSnapshot,
        )
    }

    override suspend fun appendIndex(
        localSnapshot: LocalSnapshot
    ){
        val oldGroup= courseDao.getCheckPointSnapshot(localSnapshot.refId).indexIdGroup
        val newGroup = oldGroup + localSnapshot.indexIdGroup
        courseDao.updateSnapshot(localSnapshot.refId, localSnapshot.copy(indexIdGroup = newGroup))
    }

    override suspend fun removeIndex(
        localSnapshot: LocalSnapshot
    ) {
        val oldGroup= courseDao.getCheckPointSnapshot(localSnapshot.refId).indexIdGroup
        val newGroup = oldGroup - localSnapshot.indexIdGroup.toSet()
        courseDao.updateSnapshot(localSnapshot.refId, localSnapshot.copy(indexIdGroup = newGroup))
    }
}