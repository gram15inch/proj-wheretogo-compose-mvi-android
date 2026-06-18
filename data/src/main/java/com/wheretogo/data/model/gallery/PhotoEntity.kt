package com.wheretogo.data.model.gallery

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "gallery_photo",
    indices = [Index(value = ["sourceKey"], unique = true)],
)
data class PhotoEntity(
    @PrimaryKey val id: Long,
    val imageId: String,
    val fileName: String,
    val sourceKey: String,        // 중복 판단용
    val dateTaken: Long?,
    val width: Int?,
    val height: Int?,
    val latitude: Double?,
    val longitude: Double?,
)