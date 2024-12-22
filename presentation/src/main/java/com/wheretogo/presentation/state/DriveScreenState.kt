package com.wheretogo.presentation.state

import androidx.compose.ui.text.input.TextFieldValue
import com.wheretogo.domain.model.dummy.getEmogiDummy
import com.wheretogo.domain.model.map.Comment
import com.wheretogo.domain.model.map.Course
import com.wheretogo.presentation.CommentType
import com.wheretogo.presentation.model.MapOverlay


data class DriveScreenState(
    val mapState: MapState = MapState(),
    val listState: ListState = ListState(),
    val popUpState: PopUpState = PopUpState(),
    val floatingButtonState: FloatingButtonState = FloatingButtonState(),
    val isLoading: Boolean = true,
    val error: String? = null
) {
    data class MapState(
        val isMapReady: Boolean = false,
        val mapOverlayGroup: Set<MapOverlay> = emptySet()
    )

    data class ListState(
        val isVisible: Boolean = true,
        val listItemGroup: List<ListItemState> = emptyList(),
        val clickItem: ListItemState = ListItemState()
    ) {
        data class ListItemState(
            val distanceFromCenter: Int = 0,
            val isBookmark: Boolean = false,
            val course: Course = Course()
        )
    }

    data class PopUpState(
        val isVisible: Boolean = false,
        val checkPointId: String = "",
        val localImageUrl: String = "",
        val commentState: CommentState = CommentState()
    ) {
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
                val isUserCreated: Boolean = false
            )

            data class CommentAddState(
                val commentId: String = "",
                val groupId: String = "",
                val largeEmoji: String = "",
                val emogiGroup: List<String> = getEmogiDummy(), //todo 복원하기 emptyList(),
                val oneLineReview: String = "",
                val detailReview: String = "",
                val oneLinePreview: String = "",
                val isLargeEmogi: Boolean = true,
                val isEmogiGroup: Boolean = true,
                val commentType: CommentType = CommentType.ONE,
                val editText: TextFieldValue = TextFieldValue()
            )
        }
    }

    data class FloatingButtonState(
        val isFoldVisible: Boolean = false,
        val isCommentVisible: Boolean = false,
        val isExportVisible: Boolean = false,
        val isBackPlateVisible: Boolean = false
    )
}