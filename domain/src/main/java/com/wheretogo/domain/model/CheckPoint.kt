package com.wheretogo.domain.model


data class CheckPoint(
    val id: Int = -1,
    val latLng: LatLng = LatLng(),
    val comment: String = "",
    val url: String = "",
    val userId: Int = -1,
    val like: Int = 0,
    val disLike: Int = 0
)