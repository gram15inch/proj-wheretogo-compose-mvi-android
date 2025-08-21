package com.wheretogo.domain.model.map

data class CheckPoint(
    val checkPointId: String = "",
    val courseId: String = "",
    val userId: String = "",
    val userName: String = "",
    val latLng: LatLng = LatLng(),
    val captionId: String = "",
    val caption: String = "",
    val imageName: String = "",
    val imageLocalPath: String = "",
    val description: String = ""
)