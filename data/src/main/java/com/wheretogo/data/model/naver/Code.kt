package com.wheretogo.data.model.naver

import kotlinx.serialization.Serializable

@Serializable
data class Code(
    val id: String,
    val mappingId: String,
    val type: String
)