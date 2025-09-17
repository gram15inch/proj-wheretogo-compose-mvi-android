package com.wheretogo.domain.usecase.course

import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.course.Course

interface GetNearByCourseUseCase {
    suspend operator fun invoke(center: LatLng, zoom: Double): List<Course>
}