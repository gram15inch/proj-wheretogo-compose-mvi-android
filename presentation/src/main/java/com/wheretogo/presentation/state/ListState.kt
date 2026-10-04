package com.wheretogo.presentation.state

import com.wheretogo.domain.model.course.CourseRenderItem

data class ListState(
    val listItemGroup: List<ListItemState> = emptyList()
) {
    data class ListItemState(
        val isHighlight: Boolean = false,
        val courseRenderItem: CourseRenderItem
    )
}