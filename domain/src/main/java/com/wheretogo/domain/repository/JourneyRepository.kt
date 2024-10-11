package com.wheretogo.domain.repository

import com.wheretogo.domain.model.Course
import com.wheretogo.domain.model.Journey
import com.wheretogo.domain.model.ViewPort


interface JourneyRepository {
    suspend fun getJourneys(): List<Journey>
    suspend fun getJourney(course: Course): Journey
    suspend fun getJourneyInViewPort(viewPort: ViewPort): List<Journey>
    suspend fun setJourney(map: Journey)
}