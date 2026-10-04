package com.wheretogo.data.model.course

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.lang.reflect.Type

@Entity(
    tableName = "courses",
    indices = [Index(value = ["routeId"], unique = true)],
)
data class CourseEntity(
    @PrimaryKey val id: String,
    val userName: String,
    val title: String,
    val uid: String,
    val type: String,
    val level: String,
    val tags: List<String>,
    val routeId: String,
    val fWaypoints: List<LatLngColumns>,
    val bWaypoints: List<LatLngColumns>,
    @Embedded(prefix = "center_") val center: LatLngColumns,
    @Embedded(prefix = "bounds_") val bounds: BoundsColumns,
    @Embedded(prefix = "distance_") val distanceM: LegInt,
    @Embedded(prefix = "duration_") val durationMils: LegLong,
    val reportedCount: Int,
    val hide: Boolean,
    val updateAt: Long,
    val createAt: Long,
)

data class LatLngColumns(val lat: Double, val lng: Double)

data class BoundsColumns(
    val swLat: Double,
    val swLng: Double,
    val neLat: Double,
    val neLng: Double,
)

data class LegInt(val forward: Int, val backward: Int)
data class LegLong(val forward: Long, val backward: Long)

@Entity(tableName = "sync_state")
data class SyncStateEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val cursor: Long,
    val lastSyncedAt: Long?,
) {
    companion object {
        const val SINGLETON_ID = 0
    }
}

class CourseConverters {
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val latLngListType: Type =
        Types.newParameterizedType(List::class.java, LatLngColumns::class.java)
    private val latLngGroupAdapter = moshi.adapter<List<LatLngColumns>>(latLngListType)

    @TypeConverter
    fun fromTags(tags: List<String>): String = tags.joinToString(SEP)

    @TypeConverter
    fun toTags(raw: String): List<String> = if (raw.isEmpty()) emptyList() else raw.split(SEP)

    @TypeConverter
    fun fromLatLngList(latLngList: List<LatLngColumns>?): String? {
        return latLngList?.let { latLngGroupAdapter.toJson(it) }
    }

    @TypeConverter
    fun toLatLngList(jsonString: String?): List<LatLngColumns>? {
        return jsonString?.let { latLngGroupAdapter.fromJson(it) }
    }

    private companion object {
        const val SEP = "\u001F"
    }
}
