package com.wheretogo.domain.model.checkpoint

import com.wheretogo.domain.model.address.LatLng

data class CheckPoint(
    val checkPointId: String = "",
    val courseId: String = "",
    val userId: String = "",
    val userName: String = "",
    val latLng: LatLng = LatLng(),
    val captionId: String = "",
    val caption: String = "",
    val imageId: String = "",
    val thumbnail: String = "",
    val description: String = "",
    val isUserCreated: Boolean = false
)