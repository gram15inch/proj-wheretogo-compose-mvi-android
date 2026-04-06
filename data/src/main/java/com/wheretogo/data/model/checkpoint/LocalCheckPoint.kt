package com.wheretogo.data.model.checkpoint

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.wheretogo.data.CachePolicy
import com.wheretogo.data.CheckpointPolicy
import com.wheretogo.data.DATA_NULL
import com.wheretogo.data.model.map.DataLatLng
import timber.log.Timber

@Entity(
    tableName = "LocalCheckPoint"
)
data class LocalCheckPoint(
    @PrimaryKey
    val checkPointId: String = DATA_NULL,
    val courseId: String = "",
    val userId: String = "",
    val userName: String = "",
    val latLng: DataLatLng = DataLatLng(),
    val captionId: String = "",
    val caption: String = "",
    val imageId: String = "",
    val description: String = "",
    val reportedCount: Int = 0,
    val isHide: Boolean = false,
    val updateAt: Long = 0,
    val createAt: Long = 0L
)

@Entity(tableName = "CheckPointGroupMeta")
data class CheckPointGroupMeta(
    @PrimaryKey val groupId: String,
    val cachedAt: Long
)

fun CheckPointGroup.isExpired(cachePolicy: CachePolicy): Boolean{
    val now = System.currentTimeMillis()
    val old = meta.cachedAt
    val num =
        (now - meta.cachedAt).toFloat() / (1000 * 60 * if (old == 0L) CheckpointPolicy.minuteWhenEmpty else CheckpointPolicy.minuteWhenNotEmpty)
    val formatStr = String.format("%.1f%%", num * 100)
    Timber.d("cp expire: $formatStr")
    return cachePolicy.isExpired(old,items.isEmpty()).apply {
        if(this)
            println("cp expire: $formatStr: ${meta.groupId}")
    }
}


data class CheckPointGroup(
    @Embedded val meta: CheckPointGroupMeta,
    @Relation(
        parentColumn = "groupId",
        entityColumn = "courseId"
    )
    val items: List<LocalCheckPoint>
)