package com.wheretogo.domain.usecase

import com.wheretogo.domain.model.Course
import com.wheretogo.domain.model.Journey

interface GetJourneyUseCase {
    suspend operator fun invoke(course: Course): Journey
}