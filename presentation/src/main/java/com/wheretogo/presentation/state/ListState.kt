package com.wheretogo.presentation.state

import com.wheretogo.domain.model.course.Course

data class ListState(
    val listItemGroup: List<ListItemState> = emptyList()
) {
    data class ListItemState(
        val distanceFromCenter: Int = 0,
        val course: Course = Course()
    )
}