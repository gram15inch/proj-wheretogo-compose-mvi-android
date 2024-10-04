package com.dhkim139.wheretogo.data.datasource.database

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.dhkim139.wheretogo.data.model.map.Course
import com.dhkim139.wheretogo.data.model.map.LocalMap
import com.dhkim139.wheretogo.domain.model.LatLng
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.lang.reflect.Type

@TypeConverters(MapJsonConverters::class)
@Database(entities = [LocalMap::class], version = 1, exportSchema = true)
abstract class MapDatabase : RoomDatabase() {
    abstract fun mapDao(): MapDao
}

@Dao
interface MapDao{
    @Query("SELECT * FROM LocalMap")
    suspend fun selectAll(): List<LocalMap>

    @Query("SELECT * FROM LocalMap WHERE code = :code")
    suspend fun select(code:Int): LocalMap?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: LocalMap)

}

class MapJsonConverters {

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val latLngListType: Type = Types.newParameterizedType(List::class.java, LatLng::class.java)
    private val latLngAdapter = moshi.adapter<List<LatLng>>(latLngListType)
    private val courseAdapter = moshi.adapter(Course::class.java)

    @TypeConverter
    fun fromLatLngList(latLngList: List<LatLng>?): String? {
        return latLngList?.let { latLngAdapter.toJson(it) }
    }

    @TypeConverter
    fun toLatLngList(jsonString: String?): List<LatLng>? {
        return jsonString?.let { latLngAdapter.fromJson(it) }
    }

    @TypeConverter
    fun fromCourse(course: Course?): String? {
        return course?.let { courseAdapter.toJson(course) }
    }

    @TypeConverter
    fun toCourse(jsonString: String?): Course? {
        return jsonString?.let { courseAdapter.fromJson(it) }
    }
}