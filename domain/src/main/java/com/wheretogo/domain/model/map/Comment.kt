package com.wheretogo.domain.model.map

data class Comment(
    val commentId: Int = -1,
    val userId: Int = -1,
    val checkpointId: Int = -1,
    val imoge: String = "",
    val detailedReview: String = "",
    val singleLineReview: String = "",
    val date: Long = -1,
    val like: Int = 0,
    val isLike: Boolean = false,
    val isFold: Boolean = false
)
