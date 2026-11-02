package com.wheretogo.data.model.naver

import kotlinx.serialization.Serializable

@Serializable
data class Area1(
    val alias: String,
    val coords: Coords,
    val name: String
)