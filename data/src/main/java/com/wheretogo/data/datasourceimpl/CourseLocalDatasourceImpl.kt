package com.wheretogo.data.datasourceimpl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.wheretogo.data.datasource.CourseLocalDatasource
import com.wheretogo.data.datasourceimpl.database.CourseDatabase
import com.wheretogo.data.feature.dataErrorCatching
import com.wheretogo.data.model.course.LocalCourse
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Named

class CourseLocalDatasourceImpl @Inject constructor(
    private val courseDatabase: CourseDatabase,
    @Named("contentDataStore") private val contentDataStore: DataStore<Preferences>
) : CourseLocalDatasource {
    private val courseDao by lazy { courseDatabase.courseDao() }
    private val courseCacheKey = longPreferencesKey("courseCache")
    override suspend fun getCourse(courseId: String): Result<LocalCourse?> {
        return runCatching {
            courseDao.select(courseId)
        }
    }

    override suspend fun setCourse(courseGroup: List<LocalCourse>): Result<Unit> {
        return runCatching { courseDao.insert(courseGroup) }
    }

    override suspend fun removeCourse(courseId: String): Result<Unit> {
        return runCatching { courseDao.delete(courseId) }
    }

    override suspend fun getCourseGroupByGeoHash(geoHash: String): Result<List<LocalCourse>> {
        return runCatching { courseDao.selectByGeoHash(geoHash) }
    }

    override suspend fun clear(): Result<Unit> {
        return dataErrorCatching {
            setLatestUpdate(0)
            courseDatabase.clearAllTables()
        }
    }

    override suspend fun getCourseByIsHide(isHide: Boolean): Result<List<LocalCourse>> {
        return dataErrorCatching {
            courseDao.selectByIsHide(isHide)
        }
    }


    override suspend fun getLatestUpdate(): Result<Long> {
        return dataErrorCatching {
            contentDataStore.data.map { preferences ->
                preferences[courseCacheKey] ?: 0
            }.first()
        }
    }

    override suspend fun setLatestUpdate(updateAt: Long): Result<Unit> {
        return dataErrorCatching {
            contentDataStore.edit { preferences ->
                preferences[courseCacheKey] = updateAt
            }
            Unit
        }
    }
}