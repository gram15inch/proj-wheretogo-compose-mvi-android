package com.wheretogo.presentation.state

import androidx.compose.ui.text.input.TextFieldValue
import com.wheretogo.domain.model.map.Comment
import com.wheretogo.presentation.CommentType

data class CommentState(
    val isCommentVisible: Boolean = false,
    val isCommentSettingVisible: Boolean = false,
    val selectedCommentSettingItem: CommentItemState = CommentItemState(),
    val commentItemGroup: List<CommentItemState> = emptyList(),
    val commentAddState: CommentAddState = CommentAddState()
) {
    data class CommentItemState(
        val data: Comment = Comment(),
        val isLike: Boolean = false,
        val isFold: Boolean = true,
        val isUserCreated: Boolean = false,
        val isFocus: Boolean = false
    )

    data class CommentAddState(
        //코멘트 ID 유즈케이스에서 생성
        val groupId: String = "",
        val largeEmoji: String = "",
        val emogiGroup: List<String> = emptyList(),
        val oneLineReview: String = "",
        val detailReview: String = "",
        val oneLinePreview: String = "",
        val isLargeEmogi: Boolean = true,
        val isEmogiGroup: Boolean = true,
        val commentType: CommentType = CommentType.ONE,
        val editText: TextFieldValue = TextFieldValue()
    )
}