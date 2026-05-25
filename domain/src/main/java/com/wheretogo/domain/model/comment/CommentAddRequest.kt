package com.wheretogo.domain.model.comment

data class CommentAddRequest(
    val content: CommentContent,
    val groupId: String,
    val userId: String,
    val userName: String
) {
    fun valid(): CommentAddRequest {
        require(groupId.isNotBlank()) { "inValid groupId id" }
        require(content.oneLineReview.isNotBlank()) { "empty review" }
        return this
    }
}