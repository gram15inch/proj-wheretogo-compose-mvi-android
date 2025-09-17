package com.wheretogo.domain.model.course

import com.wheretogo.domain.model.user.Profile

data class CourseAddRequest(
    val profile: Profile,
    val content: CourseContent,
    val keyword: List<String> = emptyList(),
) {
    fun valid(): CourseAddRequest {
        require(profile.uid.isNotBlank()) { "inValid user id" }
        require(content.courseName.isNotBlank()) { "inValid groupId id" }
        require(keyword.isNotEmpty()) { "empty keyword" }
        require(content.waypoints.isNotEmpty()) { "empty waypoints" }
        require(content.points.isNotEmpty()) { "empty points" }
        return this
    }
}