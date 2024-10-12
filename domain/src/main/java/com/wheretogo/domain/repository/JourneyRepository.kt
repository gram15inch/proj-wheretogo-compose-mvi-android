package com.wheretogo.domain.repository

import com.wheretogo.domain.model.Course
import com.wheretogo.domain.model.Journey
import com.wheretogo.domain.model.Viewport


interface JourneyRepository {
    suspend fun getJourneys(): List<Journey>
    suspend fun getJourney(course: Course): Journey
    suspend fun getJourneyInViewPort(viewPort: Viewport): List<Journey>
    suspend fun setJourney(map: Journey)
}