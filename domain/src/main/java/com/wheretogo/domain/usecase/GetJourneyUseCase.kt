package com.wheretogo.domain.usecase

import com.wheretogo.domain.model.map.Course
import com.wheretogo.domain.model.map.Journey

interface GetJourneyUseCase {
    suspend operator fun invoke(course: Course): Journey
}