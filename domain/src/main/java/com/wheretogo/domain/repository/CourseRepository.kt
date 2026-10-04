package com.wheretogo.domain.repository

import com.wheretogo.domain.model.course.Course
import com.wheretogo.domain.model.course.GeoBounds
import com.wheretogo.domain.usecaseimpl.course.SyncResult
import kotlinx.coroutines.flow.Flow

interface CourseRepository {
    suspend fun getById(courseId: String): Course?

    suspend fun getByTitle(title: String): List<Course>

    suspend fun getAll(): List<Course>

    fun observeInbounds(geoBounds: GeoBounds): Flow<List<Course>>

    suspend fun removeCourse(courseId: String)

    suspend fun fetchPage(force: Boolean): SyncResult

    suspend fun clearCache(): Result<Unit>
}