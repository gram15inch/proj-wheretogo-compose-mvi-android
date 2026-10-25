package com.dhkim139.feature.navigationoption.domain

import com.dhkim139.feature.navigationoption.CourseDirection
import com.dhkim139.feature.navigationoption.ui.CourseParking

internal fun interface GetCourseParkingUseCase {
    suspend operator fun invoke(
        courseId: String,
        direction: CourseDirection,
    ): Result<CourseParking>
}

internal fun interface GetMapAppIdUseCase {
    suspend operator fun invoke(): String?
}

internal fun interface SaveMapAppIdUseCase {
    suspend operator fun invoke(id: String)
}

internal class ParkingUnavailableException(val reason: Reason) :
    Exception("parking unavailable: $reason") {
    enum class Reason {
        PENDING,
        FAILED,
    }
}
