package com.wheretogo.domain.usecase.course

import com.wheretogo.domain.model.course.Course
import com.wheretogo.domain.model.course.CourseContent

interface AddCourseUseCase{
    suspend operator fun invoke(content: CourseContent): Result<Course>
}