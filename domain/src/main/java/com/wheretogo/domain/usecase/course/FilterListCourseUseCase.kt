package com.wheretogo.domain.usecase.course

import com.wheretogo.domain.model.course.Course
import com.wheretogo.domain.model.util.Viewport

interface FilterListCourseUseCase {
    suspend operator fun invoke(
        vp: Viewport,
        zoom: Double,
        courseGroup: List<Course>
    ): Result<List<Course>>
}