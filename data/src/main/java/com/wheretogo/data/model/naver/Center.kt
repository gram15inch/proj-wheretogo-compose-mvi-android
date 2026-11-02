package com.wheretogo.data.model.naver

import kotlinx.serialization.Serializable

@Serializable
data class Center(
    val crs: String,
    val x: Double,
    val y: Double
)