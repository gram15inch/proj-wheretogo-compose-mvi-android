package com.wheretogo.domain.model.comment

data class CommentAddRequest(
    val groupId: String,
    val content: CommentContent
) {
    fun valid(): CommentAddRequest {
        require(groupId.isNotBlank()) { "inValid groupId id" }
        require(content.oneLineReview.isNotBlank()) { "empty review" }
        return this
    }
}