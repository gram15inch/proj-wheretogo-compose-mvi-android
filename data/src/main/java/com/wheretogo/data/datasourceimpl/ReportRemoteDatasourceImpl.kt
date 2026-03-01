package com.wheretogo.data.datasourceimpl

import com.wheretogo.data.datasource.ReportRemoteDatasource
import com.wheretogo.data.datasourceimpl.service.ReportApiService
import com.wheretogo.data.feature.mapDataError
import com.wheretogo.data.model.firebase.DataResponse
import com.wheretogo.data.model.report.ReportRequest
import com.wheretogo.data.model.report.ReportResponse
import com.wheretogo.data.toDataError
import retrofit2.Response
import javax.inject.Inject

class ReportRemoteDatasourceImpl @Inject constructor(
    private val reportApiService: ReportApiService
) : ReportRemoteDatasource {

    override suspend fun addReport(request: ReportRequest): Result<String> {
        return safeApiCall {
            reportApiService.addReport(request)
        }.mapDataError()
    }

    override suspend fun getReport(reportId: String): Result<ReportResponse> {
        return safeApiCall {
            reportApiService.getReport(reportId)
        }.mapDataError()
    }

    suspend fun <T> safeApiCall(
        call: suspend () -> Response<DataResponse<T>>
    ): Result<T> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception("네트워크의 결과값이 없습니다."))
                }
            } else {
                return Result.failure(response.toDataError())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}