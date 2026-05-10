package com.wheretogo.domain.model.comment

data class CommentContent(
    val checkPointId: String = "",
    val emoji: String = "",
    val oneLineReview: String = "",
    val detailedReview: String = "",
    val like: Int = 0
)