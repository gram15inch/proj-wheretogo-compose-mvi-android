package com.wheretogo.domain.usecase

import com.wheretogo.domain.model.map.Course
import com.wheretogo.domain.model.map.LatLng

interface GetNearByCourseUseCase {
    suspend operator fun invoke(current: LatLng): List<Course>
}