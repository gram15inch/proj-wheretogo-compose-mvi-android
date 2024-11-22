package com.wheretogo.domain.repository

import com.wheretogo.domain.model.map.Course
import com.wheretogo.domain.model.map.Journey
import com.wheretogo.domain.model.map.Viewport


interface JourneyRepository {
    suspend fun getJourneys(size:Int): List<Journey>
    suspend fun getJourney(course: Course): Journey
    suspend fun getJourneyInViewPort(viewPort: Viewport): List<Journey>
    suspend fun setJourney(map: Journey)
    suspend fun fetchJourneyWithoutPoints()
}