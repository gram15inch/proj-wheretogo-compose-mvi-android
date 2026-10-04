package com.wheretogo.data.datasourceimpl.database

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.TypeConverters
import androidx.room.Upsert
import com.wheretogo.data.model.course.CourseConverters
import com.wheretogo.data.model.course.CourseEntity
import com.wheretogo.data.model.course.SyncStateEntity
import com.wheretogo.data.model.route.RoutePathEntity
import kotlinx.coroutines.flow.Flow

@TypeConverters(CourseConverters::class)
@Database(
    entities = [
        CourseEntity::class,
        RoutePathEntity::class,
        SyncStateEntity::class,
    ],
    version = 4,
    exportSchema = false
)

abstract class CourseDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun routePathDao(): RoutePathDao
    abstract fun syncStateDao(): SyncStateDao
}


@Dao
interface CourseDao {

    @Query("SELECT * FROM courses WHERE id ==:courseId")
    fun select(courseId:String): CourseEntity?

    @Query("SELECT * FROM courses WHERE title LIKE '%' || :title || '%' ESCAPE '\\'")
    fun selectByTitle(title:String): List<CourseEntity>

    @Query("SELECT * FROM courses ORDER BY updateAt DESC")
    suspend fun selectAll(): List<CourseEntity>


    @Query("SELECT * FROM courses ORDER BY updateAt DESC")
    fun observeAll(): Flow<List<CourseEntity>>

    @Query(
        """
        SELECT * FROM courses
        WHERE bounds_swLat <= :neLat AND bounds_neLat >= :swLat
          AND bounds_swLng <= :neLng AND bounds_neLng >= :swLng
        """,
    )
    suspend fun inBounds(swLat: Double, swLng: Double, neLat: Double, neLng: Double): List<CourseEntity>

    @Query(
        """
        SELECT * FROM courses
        WHERE bounds_swLat <= :neLat AND bounds_neLat >= :swLat
          AND bounds_swLng <= :neLng AND bounds_neLng >= :swLng
        """,
    )
    fun observeInBounds(swLat: Double, swLng: Double, neLat: Double, neLng: Double): Flow<List<CourseEntity>>

    @Upsert
    suspend fun upsert(courses: List<CourseEntity>)

    @Query("DELETE FROM courses WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    @Query("DELETE FROM courses")
    suspend fun deleteAll()
}

@Dao
interface RoutePathDao {

    @Query("SELECT * FROM route_paths WHERE routeId == :routeId")
    fun select(routeId: String): RoutePathEntity?

    @Query("SELECT * FROM route_paths WHERE routeId IN (:routeIds)")
    fun observe(routeIds: List<String>): Flow<List<RoutePathEntity>>

    @Query("SELECT routeId FROM route_paths WHERE routeId IN (:routeIds)")
    suspend fun existing(routeIds: List<String>): List<String>

    @Upsert
    suspend fun upsert(path: RoutePathEntity)

    @Query("UPDATE route_paths SET lastUsedAt = :now WHERE routeId IN (:routeIds)")
    suspend fun touch(routeIds: List<String>, now: Long)

    @Query(
        """
        DELETE FROM route_paths WHERE routeId IN (
            SELECT routeId FROM route_paths ORDER BY lastUsedAt DESC LIMIT -1 OFFSET :keep
        )
        """,
    )
    suspend fun trimTo(keep: Int)
}


@Dao
interface SyncStateDao {

    @Query("SELECT * FROM sync_state WHERE id = ${SyncStateEntity.SINGLETON_ID}")
    suspend fun get(): SyncStateEntity?

    @Query("SELECT * FROM sync_state WHERE id = ${SyncStateEntity.SINGLETON_ID}")
    fun observe(): Flow<SyncStateEntity?>


    // 행이 없을 때만 생성 (있으면 무시)
    @Query(
        """
        INSERT OR IGNORE INTO sync_state(id, cursor, lastSyncedAt)
        VALUES(${SyncStateEntity.SINGLETON_ID}, :cursor, :at)
        """,
    )
    suspend fun insertIfAbsent(cursor: Long, at: Long?)

    @Query(
        """
        UPDATE sync_state SET cursor = :cursor
        WHERE id = ${SyncStateEntity.SINGLETON_ID}
        """,
    )
    suspend fun updateCursor(cursor: Long)

    @Query(
        """
        UPDATE sync_state SET cursor = :cursor, lastSyncedAt = :at
        WHERE id = ${SyncStateEntity.SINGLETON_ID}
        """,
    )
    suspend fun updateCursorAndSyncedAt(cursor: Long, at: Long)

    @Transaction
    suspend fun setCursor(cursor: Long) {
        insertIfAbsent(cursor, null)
        updateCursor(cursor)
    }

    @Transaction
    suspend fun setSynced(cursor: Long, at: Long) {
        insertIfAbsent(cursor, at)
        updateCursorAndSyncedAt(cursor, at)
    }
}