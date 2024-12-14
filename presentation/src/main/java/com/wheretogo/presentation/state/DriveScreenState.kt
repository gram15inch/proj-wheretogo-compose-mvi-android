package com.wheretogo.presentation.state

import com.wheretogo.domain.model.map.Comment
import com.wheretogo.domain.model.map.Course
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
        val isCommentVisible: Boolean = false,
        val checkPointId: String = "",
        val localImageUrl: String = "",
        val commentState: CommentState = CommentState()
    ) {
        data class CommentState(val commentItemGroup: List<CommentItemState> = emptyList()) {
            data class CommentItemState(
                val data: Comment = Comment(),
                val isLike: Boolean = false,
                val isFold: Boolean = true
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