package com.wheretogo.domain.repository

import com.wheretogo.domain.model.Course

interface CourseRepository {
    suspend fun getCourse():List<Course>
}