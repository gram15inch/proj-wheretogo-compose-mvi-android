package com.dhkim139.feature.navigationoption.domain

import com.dhkim139.feature.navigationoption.CourseDirection
import com.dhkim139.feature.navigationoption.ui.CourseParking

internal interface ParkingRepository {
    suspend fun courseParking(
        courseId: String,
        direction: CourseDirection
    ): Result<CourseParking>
}
