package com.wheretogo.data.model.gallery

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "photo",
    indices = [
        Index(value = ["sha256"], unique = true),
        Index(value = ["sourceKey"], unique = true)
    ]
)
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val imageId: String,
    val sha256: String,             // 비트 기반
    val sourceKey: String? = null,  // uri 기반 중복 판단용
    val uriString: String? = null,
    val thumbnail: String? = null,
    @Embedded val exif: ExifEntity? = null,
    val courseId: String? = null,
    val courseName: String? = null,
    val checkPointId: String? = null,
    val address: String? = null,
)