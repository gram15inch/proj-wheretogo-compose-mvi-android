package com.wheretogo.data.model.content

import kotlinx.serialization.Serializable

@Serializable
data class ContentLikeRequest(
    val groupId: String = "",
    val contentId: String,
    val like: Boolean,
)