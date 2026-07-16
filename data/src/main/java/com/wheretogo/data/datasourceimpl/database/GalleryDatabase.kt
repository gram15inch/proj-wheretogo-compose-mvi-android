package com.wheretogo.data.datasourceimpl.database

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Transaction
import com.wheretogo.data.model.gallery.PhotoEntity
import kotlinx.coroutines.flow.Flow

@Database(entities = [PhotoEntity::class], version = 1, exportSchema = false)
abstract class GalleryDatabase : RoomDatabase() {
    abstract fun photoDao(): PhotoDao
}


@Dao
interface PhotoDao {

    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertAll(photos: List<PhotoEntity>): List<Long>

    @Query(
"""
        SELECT * FROM photo
        ORDER BY (timestampMillis IS NULL), timestampMillis DESC
        """
    )
    suspend fun getAll(): List<PhotoEntity>

    @Query(
        """
        SELECT * FROM photo
        ORDER BY (timestampMillis IS NULL), timestampMillis DESC
        """
    )
    fun observePhotos(): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photo WHERE sha256 IN (:hashes)")
    suspend fun getByHashes(hashes: List<String>): List<PhotoEntity>

    @Query("SELECT sourceKey FROM photo WHERE sourceKey IN (:keys)")
    suspend fun findExistingKeys(keys: List<String>): List<String>

    @Query("SELECT * FROM photo WHERE id IN (:ids)")
    suspend fun getById(ids: Set<Long>): List<PhotoEntity>

    @Query("SELECT * FROM photo WHERE imageId IN (:ids)")
    suspend fun getByImageId(ids: Set<String>): List<PhotoEntity>

    @Query("DELETE FROM photo WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: Set<Long>)

    @Query("UPDATE photo SET stampAt = NULL WHERE imageId IN (:ids)")
    suspend fun clearStampAtByImageIds(ids: Set<String>)

    @Query(
        """
        UPDATE photo 
        SET imageId = COALESCE(:imageId, imageId),
            courseId = COALESCE(:courseId, courseId),
            courseName = COALESCE(:courseName, courseName),
            uriString = COALESCE(:uriString, uriString),
            sourceKey = COALESCE(:sourceKey, sourceKey),
            stampAt = COALESCE(:stampAt, stampAt)
        WHERE id = :id
        """
    ) suspend fun updateById(
        id: Long,
        imageId: String? = null,
        courseId: String? = null,
        courseName: String? = null,
        uriString: String? = null,
        sourceKey: String? = null,
        stampAt: Long? = null,
    )

    @Transaction
    suspend fun updatePhotos(photos: List<PhotoEntity>) :List<Long> {
        return photos.map { photo ->
            updateById(
                id = photo.id,
                imageId = photo.imageId,
                courseId = photo.courseId,
                courseName = photo.courseName,
                uriString = photo.uriString,
                sourceKey = photo.sourceKey,
                stampAt = photo.stampAt,
            )
            photo.id
        }
    }
}