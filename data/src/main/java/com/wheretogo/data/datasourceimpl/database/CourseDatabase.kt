package com.wheretogo.data.datasourceimpl.database

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.wheretogo.data.model.course.DataMetaCheckPoint
import com.wheretogo.data.model.course.LocalCourse
import com.wheretogo.data.model.meta.LocalMetaGeoHash
import com.wheretogo.domain.model.map.LatLng
import java.lang.reflect.Type

@TypeConverters(CourseJsonConverters::class)
@Database(
    entities = [LocalCourse::class, LocalMetaGeoHash::class],
    version = 1,
    exportSchema = false
)
abstract class CourseDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao

    companion object {
        fun getInstance(context: Context): CourseDatabase {
            return Room.databaseBuilder(
                context,
                CourseDatabase::class.java,
                "course_db"
            ).fallbackToDestructiveMigration()
                .build()
        }
    }
}

@Dao
interface CourseDao {

    @Query("SELECT * FROM LocalCourse LIMIT :size")
    suspend fun selectAll(size: Int): List<LocalCourse>

    @Query("SELECT * FROM LocalCourse WHERE courseId = :courseId")
    suspend fun select(courseId: String): LocalCourse?

    @Query("SELECT * FROM LocalCourse WHERE geoHash LIKE :geoHash || '%' COLLATE NOCASE")
    suspend fun selectByGeoHash(geoHash: String): List<LocalCourse>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: LocalCourse)

    @Query("DELETE FROM LocalCourse WHERE courseId = :courseId")
    suspend fun delete(courseId: String)

    @Query("SELECT * FROM LocalMetaGeoHash")
    suspend fun getMetaGeoHashGroup(): List<LocalMetaGeoHash>

    @Query("SELECT * FROM LocalMetaGeoHash WHERE geoHash = :geoHash")
    suspend fun getMetaGeoHash(geoHash: String): LocalMetaGeoHash?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setMetaGeoHash(entity: LocalMetaGeoHash)
}

class CourseJsonConverters {

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val latLngListType: Type =
        Types.newParameterizedType(List::class.java, LatLng::class.java)
    private val latLngGroupAdapter = moshi.adapter<List<LatLng>>(latLngListType)
    private val latLngAdapter = moshi.adapter(LatLng::class.java)
    private val checkPointAdapter = moshi.adapter(DataMetaCheckPoint::class.java)

    @TypeConverter
    fun fromLatLngList(latLngList: List<LatLng>?): String? {
        return latLngList?.let { latLngGroupAdapter.toJson(it) }
    }

    @TypeConverter
    fun toLatLngList(jsonString: String?): List<LatLng>? {
        return jsonString?.let { latLngGroupAdapter.fromJson(it) }
    }

    @TypeConverter
    fun fromMetaCheckPoint(course: DataMetaCheckPoint?): String? {
        return course?.let { checkPointAdapter.toJson(course) }
    }

    @TypeConverter
    fun toMetaCheckPoint(jsonString: String?): DataMetaCheckPoint? {
        return jsonString?.let { checkPointAdapter.fromJson(it) }
    }

    @TypeConverter
    fun toLatLng(jsonString: String?): LatLng? {
        return jsonString?.let { latLngAdapter.fromJson(it) }
    }

    @TypeConverter
    fun fromLatLng(course: LatLng?): String? {
        return course?.let { latLngAdapter.toJson(course) }
    }

}