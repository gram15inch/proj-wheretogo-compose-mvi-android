package com.wheretogo.data.model.content

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ContentLikeRequest(
    val groupId: String = "",
    val contentId: String,
    val like: Boolean,
)