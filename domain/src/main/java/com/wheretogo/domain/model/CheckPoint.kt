package com.wheretogo.domain.model


data class CheckPoint(
    val id: Int = -1,
    val latLng: LatLng = LatLng(),
    val comment: String = "",
    val url: String = "/data/user/0/com.dhkim139.wheretogo/cache/thumbnails/photo_original_150x200_70.jpg", //todo 임시
    val userId: Int = -1,
    val like: Int = 0,
    val disLike: Int = 0
)