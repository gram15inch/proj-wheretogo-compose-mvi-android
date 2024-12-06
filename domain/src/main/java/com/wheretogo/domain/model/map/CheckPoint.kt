package com.wheretogo.domain.model.map

data class CheckPoint(
    val checkPointId: String = "",
    val latLng: LatLng = LatLng(),
    val titleComment: String = "",
    val imgUrl: String = "/data/user/0/com.dhkim139.wheretogo/cache/thumbnails/photo_original_150x200_70.jpg", //todo 임시
)