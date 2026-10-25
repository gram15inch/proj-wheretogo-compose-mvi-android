package com.dhkim139.feature.navigationoption.data

import com.dhkim139.feature.navigationoption.CourseDirection
import com.dhkim139.feature.navigationoption.domain.ParkingRepository
import com.dhkim139.feature.navigationoption.ui.CourseParking
import retrofit2.Response
import javax.inject.Inject

internal class ParkingRepositoryImpl @Inject constructor(
    private val api: NavOptionApi,
) : ParkingRepository {

    override suspend fun courseParking(
        courseId: String,
        direction: CourseDirection,
    ): Result<CourseParking> {
        return safeApiCall {
            api.courseParking(
                courseId = courseId,
                direction = direction.name,
                limit = 4
            )
        }.map { it.toDomain() }
    }
}


internal suspend fun <T> safeApiCall(
    call: suspend () -> Response<T>
): Result<T> {
    return try {
        val response = call()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                Result.success(body)
            } else {
                Result.failure(Exception("네트워크의 결과값이 없습니다."))
            }
        } else {
            return Result.failure(Exception("HTTP ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

