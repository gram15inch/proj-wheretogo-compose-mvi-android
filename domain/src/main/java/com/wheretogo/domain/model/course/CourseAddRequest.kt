package com.wheretogo.domain.model.course

import com.wheretogo.domain.DomainError
import com.wheretogo.domain.FieldInvalidReason
import com.wheretogo.domain.RouteFieldType

data class CourseAddRequest(
    val content: CourseContent,
    val keyword: List<String> = emptyList(),
) {
    fun valid(): CourseAddRequest {
        require(content.courseName.isNotBlank()) { "inValid groupId id" }
        when {
            keyword.isEmpty() ->
                throw DomainError.RouteFieldInvalid(RouteFieldType.KEYWORD, FieldInvalidReason.MIN)

            content.waypoints.isEmpty() || content.points.isEmpty() ->
                throw DomainError.RouteFieldInvalid(RouteFieldType.POINT, FieldInvalidReason.MIN)
        }
        return this
    }
}