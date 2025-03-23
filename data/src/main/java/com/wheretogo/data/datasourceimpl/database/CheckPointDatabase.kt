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
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.wheretogo.data.model.checkpoint.LocalCheckPoint
import com.wheretogo.domain.model.map.LatLng

@TypeConverters(CheckPointJsonConverters::class)
@Database(
    entities = [LocalCheckPoint::class],
    version = 1,
    exportSchema = false
)
abstract class CheckPointDatabase : RoomDatabase() {
    abstract fun checkPointDao(): CheckPointDao
}

@Dao
interface CheckPointDao {

    @Query("SELECT * FROM LocalCheckPoint WHERE checkPointId = :checkPointId")
    suspend fun select(checkPointId: String): LocalCheckPoint?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: LocalCheckPoint)

    @Query("DELETE FROM LocalCheckPoint WHERE checkPointId = :checkPointId")
    suspend fun delete(checkPointId: String)

    @Query("UPDATE LocalCheckPoint SET caption =:caption WHERE checkPointId = :checkPointId")
    suspend fun update(checkPointId: String, caption: String)

}

class CheckPointJsonConverters {

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val latLngAdapter = moshi.adapter(LatLng::class.java)

    @TypeConverter
    fun toLatLng(jsonString: String?): LatLng? {
        return jsonString?.let { latLngAdapter.fromJson(it) }
    }

    @TypeConverter
    fun fromLatLng(course: LatLng?): String? {
        return course?.let { latLngAdapter.toJson(course) }
    }

}