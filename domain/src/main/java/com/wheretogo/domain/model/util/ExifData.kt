package com.wheretogo.domain.model.util

import com.wheretogo.domain.model.address.LatLng

data class ExifData(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val altitude: Double? = null,          // 고도(m)
    val dateTimeOriginal: String? = null,  // 촬영 시각 (원본 문자열)
    val timestampMillis: Long? = null,     // 촬영 시각 (epoch millis)
    val make: String? = null,              // 제조사
    val model: String? = null,             // 모델명
    val orientation: Int? = null,          // 회전 정보
    val imageWidth: Int? = null,           // orientation 적용
    val imageHeight: Int? = null,          // orientation 적용
    val fNumber: String? = null,           // 조리개
    val exposureTime: String? = null,      // 셔터스피드
    val iso: String? = null,
    val focalLength: String? = null
) {
    val getLatLng: LatLng?
        get() = if (latitude != null && longitude != null) LatLng(latitude, longitude) else null
}