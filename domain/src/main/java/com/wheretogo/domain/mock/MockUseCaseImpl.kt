package com.wheretogo.domain.mock

import com.wheretogo.domain.model.map.Course
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.usecase.FetchJourneyWithoutPointsUseCase
import com.wheretogo.domain.usecase.GetNearByCourseUseCase
import javax.inject.Inject

class MockUseCaseImpl @Inject constructor() :
    GetNearByCourseUseCase,
    FetchJourneyWithoutPointsUseCase {

    override suspend fun invoke(current: LatLng): List<Course> = emptyList()

    override suspend fun invoke() {}
}