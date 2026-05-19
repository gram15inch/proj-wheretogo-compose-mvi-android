package com.wheretogo.presentation.state

import com.wheretogo.domain.model.course.Course

data class ListState(
    val listItemGroup: List<ListItemState> = emptyList()
) {
    data class ListItemState(
        val isHighlight: Boolean = false,
        val course: Course = Course()
    )
}