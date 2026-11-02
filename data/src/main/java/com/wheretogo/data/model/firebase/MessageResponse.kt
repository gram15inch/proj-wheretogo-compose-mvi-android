package com.wheretogo.data.model.firebase

import kotlinx.serialization.Serializable

@Serializable
data class MessageResponse(
    val success: Boolean,
    val message: String = ""
)