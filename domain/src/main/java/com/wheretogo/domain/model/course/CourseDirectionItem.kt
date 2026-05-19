package com.wheretogo.domain.model.course

enum class StartDirection{
    FORWARD, REVERSE
}

data class CourseDirectionItem(
    val course: Course,
    val direction: StartDirection = StartDirection.FORWARD
)