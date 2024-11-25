package com.wheretogo.data.datasource.database

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
import com.wheretogo.data.model.journey.LocalCourse
import com.wheretogo.data.model.journey.LocalJourney
import com.wheretogo.data.model.journey.LocalLatLng
import java.lang.reflect.Type

@TypeConverters(JourneyJsonConverters::class)
@Database(entities = [LocalJourney::class], version = 4, exportSchema = false)
abstract class JourneyDatabase : RoomDatabase() {
    abstract fun journeyDao(): JourneyDao

    companion object {
        fun getInstance(context: Context): JourneyDatabase {
            return Room.databaseBuilder(
                context,
                JourneyDatabase::class.java,
                "journey_db"
            ).fallbackToDestructiveMigration()
                .build()
        }
    }
}

@Dao
interface JourneyDao {
    @Query("SELECT * FROM LocalJourney LIMIT :size")
    suspend fun selectAll(size: Int): List<LocalJourney>

    @Query("SELECT * FROM LocalJourney WHERE code = :code")
    suspend fun select(code: Int): LocalJourney?

    @Query("SELECT * FROM LocalJourney WHERE latitude BETWEEN :minLatitude AND :maxLatitude AND longitude BETWEEN :minLongitude AND :maxLongitude")
    suspend fun selectInViewPort(
        minLatitude: Double,
        maxLatitude: Double,
        minLongitude: Double,
        maxLongitude: Double
    ): List<LocalJourney>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: LocalJourney)

}

class JourneyJsonConverters {

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val latLngListType: Type =
        Types.newParameterizedType(List::class.java, LocalLatLng::class.java)
    private val latLngAdapter = moshi.adapter<List<LocalLatLng>>(latLngListType)
    private val courseAdapter = moshi.adapter(LocalCourse::class.java)

    @TypeConverter
    fun fromLatLngList(latLngList: List<LocalLatLng>?): String? {
        return latLngList?.let { latLngAdapter.toJson(it) }
    }

    @TypeConverter
    fun toLatLngList(jsonString: String?): List<LocalLatLng>? {
        return jsonString?.let { latLngAdapter.fromJson(it) }
    }

    @TypeConverter
    fun fromCourse(course: LocalCourse?): String? {
        return course?.let { courseAdapter.toJson(course) }
    }

    @TypeConverter
    fun toCourse(jsonString: String?): LocalCourse? {
        return jsonString?.let { courseAdapter.fromJson(it) }
    }
}