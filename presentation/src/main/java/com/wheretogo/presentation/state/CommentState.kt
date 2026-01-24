package com.wheretogo.presentation.state

import com.wheretogo.domain.model.comment.Comment
import com.wheretogo.presentation.CommentType
import com.wheretogo.presentation.defaultCommentEmogiGroup

data class CommentState(
    val isContentVisible: Boolean = false,
    val isImeVisible: Boolean = false,
    val isLoading: Boolean = false,
    val isDragGuide: Boolean = false,
    val commentItemGroup: List<CommentItemState> = emptyList(),
    val commentSettingState: CommentSettingState = CommentSettingState(),
    val commentAddState: CommentAddState = CommentAddState()
) {
    data class CommentItemState(
        val data: Comment = Comment(),
        val isFold: Boolean = true,
        val isLoading: Boolean = false
    )

    data class CommentAddState(
        val titleEmoji: String = defaultCommentEmogiGroup().firstOrNull() ?: "",
        val emogiGroup: List<String> = defaultCommentEmogiGroup(),
        val oneLineReview: String = "",
        val detailReview: String = "",
        val isOneLinePreview: Boolean = false,
        val isLoading: Boolean = false,
        val commentType: CommentType = CommentType.ONE
    )

    data class CommentSettingState(
        val isLoading: Boolean = false,
        val isVisible: Boolean = false,
        val comment: Comment = Comment(),
    )
}