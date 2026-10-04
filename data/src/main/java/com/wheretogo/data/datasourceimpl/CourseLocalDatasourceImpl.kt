package com.wheretogo.data.datasourceimpl

import androidx.room.withTransaction
import com.wheretogo.data.model.course.CourseEntity
import com.wheretogo.data.datasource.CourseLocalDatasource
import com.wheretogo.data.datasourceimpl.database.CourseDatabase
import com.wheretogo.domain.model.course.GeoBounds
import com.wheretogo.domain.model.course.SyncState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CourseLocalDatasourceImpl @Inject constructor(
    private val courseDatabase: CourseDatabase,
) : CourseLocalDatasource {
    private val courseDao by lazy { courseDatabase.courseDao() }
    private val syncDao by lazy { courseDatabase.syncStateDao() }

    override suspend fun upsert(courseGroup: List<CourseEntity>) {
        return courseDao.upsert(courseGroup)
    }


    override suspend fun selectById(id: String?): List<CourseEntity> {
        return if(id==null)
            courseDao.selectAll()
        else
            courseDao.select(id)?.let { listOf(it) }?:emptyList()
    }

    override suspend fun selectByTitle(title: String): List<CourseEntity> {
        return courseDao.selectByTitle(title)
    }

    override fun observeInBounds(bounds: GeoBounds): Flow<List<CourseEntity>> =
        courseDao.observeInBounds(
            swLat = bounds.swLat,
            swLng = bounds.swLng,
            neLat = bounds.neLat,
            neLng = bounds.neLng
        )


    override suspend fun applyDelta(upserts: List<CourseEntity>, deletedIds: List<String>, cursor: Long) {
        courseDatabase.withTransaction {
            if (upserts.isNotEmpty()) courseDao.upsert(upserts)
            if (deletedIds.isNotEmpty()) courseDao.deleteByIds(deletedIds)
            syncDao.setCursor(cursor)
        }
    }

    override suspend fun replaceAll(courses: List<CourseEntity>, cursor: Long, syncedAt: Long) {
        courseDatabase.withTransaction {
            courseDao.deleteAll()
            courseDao.upsert(courses)
            syncDao.setSynced(cursor, syncedAt)
        }
    }


    override suspend fun delete(courseId: String) {
        return courseDao.deleteByIds(listOf(courseId))
    }

    override suspend fun clear() {
        courseDatabase.clearAllTables()
    }


    override suspend fun syncState(): SyncState =
        syncDao.get()?.let { SyncState(it.cursor, it.lastSyncedAt) } ?: SyncState.EMPTY


    override fun observeSyncState(): Flow<SyncState> =
        syncDao.observe().map { it?.let { row -> SyncState(row.cursor, row.lastSyncedAt) } ?: SyncState.EMPTY }

    override suspend fun markSynced(cursor: Long, at: Long) = syncDao.setSynced(cursor, at)
}