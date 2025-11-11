package com.wheretogo.domain.model.course

import com.wheretogo.domain.DomainError
import com.wheretogo.domain.FieldInvalidReason
import com.wheretogo.domain.RouteFieldType
import com.wheretogo.domain.model.user.Profile

data class CourseAddRequest(
    val profile: Profile,
    val content: CourseContent,
    val keyword: List<String> = emptyList(),
) {
    fun valid(): CourseAddRequest {
        require(profile.uid.isNotBlank()) { "inValid user id" }
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