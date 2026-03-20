package com.wheretogo.data.datasourceimpl

import com.wheretogo.data.datasource.ReportRemoteDatasource
import com.wheretogo.data.datasourceimpl.service.ReportApiService
import com.wheretogo.data.feature.mapDataError
import com.wheretogo.data.feature.safeApiCall
import com.wheretogo.data.model.report.ReportRequest
import com.wheretogo.data.model.report.ReportResponse
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
}