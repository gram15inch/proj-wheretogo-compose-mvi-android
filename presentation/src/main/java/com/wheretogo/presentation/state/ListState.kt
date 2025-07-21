package com.wheretogo.presentation.state

import com.wheretogo.domain.model.map.Course

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