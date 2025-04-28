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
import com.wheretogo.data.model.map.DataLatLng
import com.wheretogo.data.model.route.LocalRoute
import java.lang.reflect.Type


@TypeConverters(RouteJsonConverters::class)
@Database(
    entities = [LocalRoute::class],
    version = 1,
    exportSchema = false
)
abstract class RouteDatabase : RoomDatabase() {
    abstract fun routeDao(): RouteDao
}

@Dao
interface RouteDao {

    @Query("SELECT * FROM LocalRoute WHERE courseId = :coureId")
    suspend fun select(coureId: String): LocalRoute?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: LocalRoute)

    @Query("DELETE FROM LocalRoute WHERE courseId = :coureId")
    suspend fun delete(coureId: String)

}

class RouteJsonConverters {

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
    fun fromLatLng(course: DataLatLng?): String? {
        return course?.let { latLngAdapter.toJson(course) }
    }

}