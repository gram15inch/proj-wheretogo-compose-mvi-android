package com.wheretogo.data.datasource

import com.wheretogo.data.model.course.CourseEntity
import com.wheretogo.domain.model.course.GeoBounds
import com.wheretogo.domain.model.course.SyncState
import kotlinx.coroutines.flow.Flow


interface CourseLocalDatasource {

    suspend fun upsert(courseGroup: List<CourseEntity>)


    suspend fun selectById(id: String? = null): List<CourseEntity>

    suspend fun selectByTitle(title: String): List<CourseEntity>

    fun observeInBounds(bounds: GeoBounds): Flow<List<CourseEntity>>


    suspend fun applyDelta(upserts: List<CourseEntity>, deletedIds: List<String>, cursor: Long)

    suspend fun replaceAll(courses: List<CourseEntity>, cursor: Long, syncedAt: Long)


    suspend fun delete(courseId: String)

    suspend fun clear()


    //=========================================
    // sync
    //=========================================
    suspend fun syncState(): SyncState

    fun observeSyncState(): Flow<SyncState>

    suspend fun markSynced(cursor: Long, at: Long)

}