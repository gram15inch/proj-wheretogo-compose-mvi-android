package com.wheretogo.domain.usecase.course

import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.course.Course
import com.wheretogo.domain.model.util.Viewport

interface GetNearByCourseUseCase {
    suspend operator fun invoke(center: LatLng, zoom: Double, viewport: Viewport): Result<List<Course>>
}