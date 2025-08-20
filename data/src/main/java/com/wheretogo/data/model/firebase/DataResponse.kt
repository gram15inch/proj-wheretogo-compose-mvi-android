package com.wheretogo.data.model.firebase

data class DataResponse<T>(
    val success: Boolean,
    val message: String = "",
    val data: T
)
