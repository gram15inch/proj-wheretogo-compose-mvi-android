package com.wheretogo.data.datasourceimpl.database

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Upsert
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.wheretogo.data.model.checkpoint.CheckPointCluster
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

    // item
    @Query("SELECT * FROM LocalCheckPoint Where checkPointId IN (:checkpointIdGroup)")
    suspend fun select(checkpointIdGroup: List<String>): List<LocalCheckPoint>

    @Upsert
    suspend fun upsert(checkpoints: List<LocalCheckPoint>)

    @Query("DELETE FROM LocalCheckPoint WHERE checkPointId IN (:checkPointIdGroup)")
    suspend fun delete(checkPointIdGroup: List<String>)


    // group
    @Transaction
    @Query("SELECT * FROM CheckPointGroupMeta WHERE groupId = :groupId")
    fun selectGroup(groupId: String): CheckPointCluster?

    @Query("DELETE FROM LocalCheckPoint WHERE courseId = :groupId")
    suspend fun deleteGroup(groupId: String)


    // meta
    @Query("SELECT * FROM CheckPointGroupMeta WHERE groupId = :groupId")
    fun selectMeta(groupId: String): CheckPointGroupMeta?

    @Upsert
    suspend fun upsertMeta(meta: CheckPointGroupMeta)

    @Query("DELETE FROM CheckPointGroupMeta WHERE groupId = :groupId")
    suspend fun deleteMeta(groupId: String)

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