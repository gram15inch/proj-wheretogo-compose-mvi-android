package com.wheretogo.domain.model

data class Comment(
    val commentId: Int = -1,
    val userId: Int = -1,
    val checkpointId: Int = -1,
    val imoge: String = "",
    val content: String = "",
    val date: Long = -1,
    val like: Int = 0
)
