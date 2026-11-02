package com.wheretogo.data.model.firebase

import kotlinx.serialization.Serializable

@Serializable
data class DataResponse<T>(
    val success: Boolean,
    val message: String = "",
    val data: T
)
