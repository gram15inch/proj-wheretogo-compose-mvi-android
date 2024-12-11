package com.wheretogo.domain.model.map

data class CheckPoint(
    val checkPointId: String = "",
    val latLng: LatLng = LatLng(),
    val titleComment: String = "",
    val remoteImgUrl: String = "",
    val localImgUrl: String = "",
)