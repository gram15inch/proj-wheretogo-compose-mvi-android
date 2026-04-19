package com.wheretogo.data.model.checkpoint

import com.wheretogo.data.model.map.DataLatLng

data class RemoteCheckPoint(
    val id: String = "",
    val courseId: String = "",
    val userId: String = "",
    val userName: String = "",
    val latLng: DataLatLng = DataLatLng(),
    val captionId: String = "",
    val caption: String = "",
    val imageId: String = "",
    val description: String = "",
    val reportedCount: Int = 0,
    val hide: Boolean = false,
    val updateAt: Long = 0,
    val createAt: Long = 0L
)