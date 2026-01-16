package com.wheretogo.domain.model.course

import com.wheretogo.domain.PathType

data class CourseAddValidContent(
    val name: String = "",
    val pathType: PathType = PathType.SCAFFOLD,
    val selectedAttrSize: Int = 0
)