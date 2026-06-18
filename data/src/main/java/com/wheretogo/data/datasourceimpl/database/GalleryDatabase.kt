package com.wheretogo.data.datasourceimpl.database

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import com.wheretogo.data.model.gallery.PhotoEntity
import kotlinx.coroutines.flow.Flow

@Database(entities = [PhotoEntity::class], version = 1, exportSchema = false)
abstract class GalleryDatabase : RoomDatabase() {
    abstract fun photoDao(): GalleryPhotoDao
}


@Dao
interface GalleryPhotoDao {

    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertAll(photos: List<PhotoEntity>): List<Long>

    @Query(
        """
        SELECT * FROM gallery_photo
        ORDER BY (dateTaken IS NULL), dateTaken DESC
        """
    )
    suspend fun getAll(): List<PhotoEntity>

    @Query("SELECT sourceKey FROM gallery_photo WHERE sourceKey IN (:keys)")
    suspend fun findExistingKeys(keys: List<String>): List<String>

    @Query("SELECT * FROM gallery_photo WHERE id IN (:ids)")
    suspend fun getByIds(ids: Set<Long>): List<PhotoEntity>

    @Query("DELETE FROM gallery_photo WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: Set<Long>)

    // 흐름 관찰이 필요하면 추가 (확장용)
    @Query(
        """
        SELECT * FROM gallery_photo
        ORDER BY (dateTaken IS NULL), dateTaken DESC
        """
    )
    fun observeAll(): Flow<List<PhotoEntity>>
}