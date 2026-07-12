package com.wheretogo.domain.usecase.course

import com.wheretogo.domain.model.course.Course

interface GetCourseUseCase {
    suspend operator fun invoke(courseId: String): Result<Course>
}