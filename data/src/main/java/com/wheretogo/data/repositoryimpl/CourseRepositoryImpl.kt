package com.wheretogo.data.repositoryimpl

import com.wheretogo.data.ApiResult
import com.wheretogo.data.model.course.CourseDto
import com.wheretogo.data.course.toDomain
import com.wheretogo.data.course.toEntities
import com.wheretogo.data.datasource.CourseLocalDatasource
import com.wheretogo.data.datasource.CourseRemoteDatasource
import com.wheretogo.domain.SyncFailureKind
import com.wheretogo.domain.model.course.Course
import com.wheretogo.domain.model.course.GeoBounds
import com.wheretogo.domain.model.course.LoadingConfig.SYNC_INTERVAL_MS
import com.wheretogo.domain.model.course.LoadingConfig.SYNC_MAX_PAGES
import com.wheretogo.domain.repository.CourseRepository
import com.wheretogo.domain.usecaseimpl.course.SyncResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

class CourseRepositoryImpl @Inject constructor(
    private val courseRemoteDatasource: CourseRemoteDatasource,
    private val courseLocalDatasource: CourseLocalDatasource
) : CourseRepository {

    override suspend fun getById(courseId: String): Course? {
        return courseLocalDatasource.selectById(courseId).firstOrNull()?.toDomain()
    }

    override suspend fun getByTitle(title: String): List<Course> {
        return courseLocalDatasource.selectByTitle(title).map { it.toDomain() }
    }

    override suspend fun getAll(): List<Course> {
        return courseLocalDatasource.selectById().map { it.toDomain() }
    }

    override fun observeInbounds(geoBounds: GeoBounds): Flow<List<Course>> {
        return courseLocalDatasource.observeInBounds(geoBounds).map { it.map { it.toDomain() } }
    }

    override suspend fun removeCourse(courseId: String) {
        courseRemoteDatasource.removeCourse(courseId)
        courseLocalDatasource.delete(courseId)
    }

    override suspend fun fetchPage(force: Boolean): SyncResult {
        val state = courseLocalDatasource.syncState()
        val startedAt = now()

        if (!force && !isDue(state.lastSyncedAt, startedAt)) {
            return SyncResult.Skipped(nextDueAt = state.lastSyncedAt!! + SYNC_INTERVAL_MS)
        }

        var cursor = state.cursor
        var upserted = 0
        var removed = 0

        repeat(SYNC_MAX_PAGES) {
            val page = courseRemoteDatasource.fetchPage(cursor).getOrElse {
                return SyncResult.Failed(it)
            }
            if (page.resync) return replaceAll()

            val next = page.cursor ?: cursor
            val entities =
                page.upserts.mapNotNull { it.toEntities(Timber::w) }
            courseLocalDatasource.applyDelta(entities, page.deletedIds, next)
            upserted += page.upserts.size
            removed += page.deletedIds.size

            if (!page.hasMore) return finish(next, upserted, removed)

            // hasMore 인데 커서가 안 늘면 서버 쪽 이상
            if (next <= cursor) {
                Timber.w("hasMore 인데 커서동일 cursor: $cursor")
                return finish(next, upserted, removed)
            }
            cursor = next
        }
        return finish(cursor, upserted, removed)
    }

    private fun isDue(last: Long?, now: Long): Boolean = when {
        last == null -> true
        now < last -> true
        else -> now - last >= SYNC_INTERVAL_MS
    }

    private suspend fun finish(cursor: Long, upserted: Int, removed: Int): SyncResult {
        courseLocalDatasource.markSynced(cursor, now())
        return SyncResult.Synced(upserted, removed)
    }

    private suspend fun replaceAll(): SyncResult {
        val all = mutableListOf<CourseDto>()
        var cursor = 0L

        repeat(SYNC_MAX_PAGES) {
            val page = courseRemoteDatasource.fetchPage(cursor).getOrElse {
                return SyncResult.Failed(it)
            }
            all += page.upserts
            val entities = all.mapNotNull { it.toEntities(Timber::w) }
            val next = page.cursor ?: cursor
            if (!page.hasMore || next <= cursor) {
                courseLocalDatasource.replaceAll(entities, next, now())
                return SyncResult.Synced(upserted = all.size, removed = 0, isRepalceAll = true)
            }
            cursor = next
        }

        return SyncResult.Failed(SyncFailureKind.PERMANENT)
    }

    private fun now(): Long {
        return System.currentTimeMillis()
    }

    private inline fun <T> ApiResult<T>.getOrElse(onFailure: (SyncFailureKind) -> Nothing): T =
        when (this) {
            is ApiResult.Success -> data
            is ApiResult.HttpError -> onFailure(SyncFailureKind.TEMPORARY)
            is ApiResult.NetworkError -> onFailure(SyncFailureKind.TEMPORARY)
            is ApiResult.UnknownError -> onFailure(SyncFailureKind.PERMANENT)
        }

    override suspend fun clearCache(): Result<Unit> {
        return runCatching {
            courseLocalDatasource.clear()
        }
    }
}

