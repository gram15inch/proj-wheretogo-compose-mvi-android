package com.wheretogo.data.model.content

data class ContentLike(
    val groupId: String = "",
    val contentId: String,
    val isLike: Boolean,
)