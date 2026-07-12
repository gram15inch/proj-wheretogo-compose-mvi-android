package com.wheretogo.data.model.gallery

data class ExifResponse(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val altitude: Double? = null,
    val dateTimeOriginal: String? = null,
    val timestampMillis: Long? = null,
    val make: String? = null,
    val model: String? = null,
    val orientation: Int? = null,
    val imageWidth: Int? = null,
    val imageHeight: Int? = null,
    val fNumber: String? = null,
    val exposureTime: String? = null,
    val iso: String? = null,
    val focalLength: String? = null,
)