package com.wheretogo.data.datasourceimpl.database

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Upsert
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.wheretogo.data.model.checkpoint.CheckPointGroup
import com.wheretogo.data.model.checkpoint.CheckPointGroupMeta
import com.wheretogo.data.model.checkpoint.LocalCheckPoint
import com.wheretogo.data.model.map.DataLatLng


@TypeConverters(CheckPointJsonConverters::class)
@Database(
    entities = [
        LocalCheckPoint::class,
        CheckPointGroupMeta::class
    ],
    version = 4,
    exportSchema = true
)
abstract class CheckPointDatabase : RoomDatabase() {
    abstract fun checkPointDao(): CheckPointDao
}

@Dao
interface CheckPointDao {

    @Query("SELECT * FROM LocalCheckPoint Where checkPointId IN (:checkpointIdGroup)")
    suspend fun select(checkpointIdGroup: List<String>): List<LocalCheckPoint>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: List<LocalCheckPoint>)

    @Query("DELETE FROM LocalCheckPoint WHERE courseId IN (:checkPointIdGroup)")
    suspend fun delete(checkPointIdGroup: List<String>)

    @Upsert
    suspend fun upsert(checkpoints: List<LocalCheckPoint>)

    @Upsert
    suspend fun upsertMeta(meta: CheckPointGroupMeta)

    @Query("SELECT * FROM CheckPointGroupMeta WHERE groupId = :groupId")
    fun selectMeta(groupId: String): CheckPointGroupMeta?

    @Transaction
    @Query("SELECT * FROM CheckPointGroupMeta WHERE groupId = :groupId")
    fun getCheckPointGroup(groupId: String): CheckPointGroup?

    @Query("DELETE FROM CheckPointGroupMeta WHERE groupId = :groupId")
    suspend fun deleteMeta(groupId: String)

    @Query("DELETE FROM LocalCheckPoint WHERE courseId = :groupId")
    suspend fun deleteGroup(groupId: String)

}

class CheckPointJsonConverters {

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val latLngAdapter = moshi.adapter(DataLatLng::class.java)

    @TypeConverter
    fun toLatLng(jsonString: String?): DataLatLng? {
        return jsonString?.let { latLngAdapter.fromJson(it) }
    }

    @TypeConverter
    fun fromLatLng(course: DataLatLng?): String? {
        return course?.let { latLngAdapter.toJson(course) }
    }

}