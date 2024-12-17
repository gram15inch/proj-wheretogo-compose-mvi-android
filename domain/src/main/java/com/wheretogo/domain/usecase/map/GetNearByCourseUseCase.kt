package com.wheretogo.domain.usecase.map

import com.wheretogo.domain.model.map.Course
import com.wheretogo.domain.model.map.LatLng

interface GetNearByCourseUseCase {
    suspend operator fun invoke(current: LatLng): List<Course>
}