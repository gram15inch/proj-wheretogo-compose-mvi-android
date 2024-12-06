package com.wheretogo.presentation.state

import com.wheretogo.domain.model.map.Comment
import com.wheretogo.domain.model.map.Course
import com.wheretogo.presentation.model.MapOverlay


data class DriveScreenState(
    val mapState: MapState = MapState(),
    val listState: ListState = ListState(),
    val popUpState: PopUpState = PopUpState(),
    val floatingButtonState: FloatingButtonState = FloatingButtonState(),
    val error: String? = null
) {
    data class MapState(
        val isMapReady: Boolean = false,
        val mapData: List<MapOverlay> = emptyList()
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
        val checkPointId: Int = -1,
        val imageUrl: String = "",
        val commentState: CommentState = CommentState()
    ) {
        data class CommentState(val data: List<Comment> = emptyList())
    }

    data class FloatingButtonState(
        val isFoldVisible: Boolean = false,
        val isCommentVisible: Boolean = false,
        val isExportVisible: Boolean = false,
        val isBackPlateVisible: Boolean = false
    )
}