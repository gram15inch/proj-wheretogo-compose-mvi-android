package com.wheretogo.domain.model.map

data class CheckPoint(
    val checkPointId: String = "",
    val userId: String = "",
    val latLng: LatLng = LatLng(),
    val titleComment: String = "",
    val imageName: String = "",
    val imageLocalPath: String = "",
    val description: String = ""
)