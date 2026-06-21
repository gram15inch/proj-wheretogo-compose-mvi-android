package com.wheretogo.data.datasourceimpl.database

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Transaction
import com.wheretogo.data.model.gallery.PhotoEntity
import com.wheretogo.domain.model.gallery.GalleryPhoto

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

    @Query(
"""
        UPDATE gallery_photo 
        SET dateTaken = COALESCE(:dateTaken, dateTaken),
            width = COALESCE(:width, width),
            height = COALESCE(:height, height),
            latitude = COALESCE(:latitude, latitude),
            longitude = COALESCE(:longitude, longitude),
            courseId = COALESCE(:courseId, longitude),
            courseName = COALESCE(:courseName, longitude)
        WHERE id = :id
        """
    )
    suspend fun updateById(
        id: Long,
        dateTaken: Long? = null,
        width: Int? = null,
        height: Int? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        courseId: String? = null,
        courseName: String? = null
    )

    @Transaction
    suspend fun updateGalleryPhotos(photos: List<GalleryPhoto>) {
        photos.forEach { photo ->
            updateById(
                id = photo.id,
                dateTaken = photo.exif.dateTaken,
                width = photo.exif.width,
                height = photo.exif.width,
                latitude = photo.exif.location?.latitude,
                longitude = photo.exif.location?.longitude,
                courseId = photo.courseId,
                courseName = photo.courseName
            )
        }
    }
}