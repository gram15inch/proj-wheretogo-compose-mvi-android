package com.wheretogo.domain.usecase.util

import com.wheretogo.domain.CourseAddValid
import com.wheretogo.domain.model.course.CourseAddValidContent

interface CourseAddValidUseCase {
    suspend operator fun invoke(
        courseAddValidContent: CourseAddValidContent
    ): Result<List<CourseAddValid>>
}