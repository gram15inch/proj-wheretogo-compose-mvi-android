package com.dhkim139.wheretogo.mock

import com.wheretogo.data.datasource.ReportRemoteDatasource
import com.wheretogo.data.feature.dataErrorCatching
import com.wheretogo.data.model.report.ReportRequest
import com.wheretogo.data.model.report.ReportResponse
import javax.inject.Inject

class MockReportRemoteDatasourceImpl @Inject constructor() : ReportRemoteDatasource {
    override suspend fun addReport(request: ReportRequest): Result<String> {
        return dataErrorCatching {
        }.mapCatching { "" }
    }

    override suspend fun getReport(reportId: String): Result<ReportResponse> {
        return dataErrorCatching { ReportResponse() }
    }
}