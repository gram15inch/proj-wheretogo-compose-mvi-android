package com.wheretogo.data.datasourceimpl.database

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.wheretogo.data.model.course.LocalCourse
import com.wheretogo.data.model.map.DataLatLng
import java.lang.reflect.Type

@TypeConverters(CourseJsonConverters::class)
@Database(
    entities = [LocalCourse::class],
    version = 3,
    exportSchema = false
)
abstract class CourseDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
}

@Dao
interface CourseDao {

    @Query("SELECT * FROM LocalCourse LIMIT :size")
    suspend fun selectAll(size: Int): List<LocalCourse>

    @Query("SELECT * FROM LocalCourse WHERE courseId = :courseId")
    suspend fun select(courseId: String): LocalCourse?

    @Query("SELECT * FROM LocalCourse WHERE geoHash LIKE :geoHash || '%' COLLATE NOCASE")
    suspend fun selectByGeoHash(geoHash: String): List<LocalCourse>

    @Query("SELECT * FROM LocalCourse WHERE isHide = :isHide")
    suspend fun selectByIsHide(isHide: Boolean): List<LocalCourse>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: List<LocalCourse>)

    @Query("DELETE FROM LocalCourse WHERE courseId = :courseId")
    suspend fun delete(courseId: String)
}

class CourseJsonConverters {

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val latLngListType: Type =
        Types.newParameterizedType(List::class.java, DataLatLng::class.java)
    private val latLngGroupAdapter = moshi.adapter<List<DataLatLng>>(latLngListType)
    private val latLngAdapter = moshi.adapter(DataLatLng::class.java)

    @TypeConverter
    fun fromLatLngList(latLngList: List<DataLatLng>?): String? {
        return latLngList?.let { latLngGroupAdapter.toJson(it) }
    }

    @TypeConverter
    fun toLatLngList(jsonString: String?): List<DataLatLng>? {
        return jsonString?.let { latLngGroupAdapter.fromJson(it) }
    }

    @TypeConverter
    fun toLatLng(jsonString: String?): DataLatLng? {
        return jsonString?.let { latLngAdapter.fromJson(it) }
    }

    @TypeConverter
    fun fromLatLng(latlng: DataLatLng?): String? {
        return latlng?.let { latLngAdapter.toJson(it) }
    }
}