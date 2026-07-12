package com.wheretogo.domain.model.gallery

data class Stamp(
    val id: String,
    val courseId: String,
    val pickerId: Long? = null,
    val thumbnail: String? = null,
    val description: String = "",
    val visitedCount: Int = 0,
    val createAt: Long = 0L,
)