package com.dhkim139.wheretogo.mock

import com.wheretogo.data.datasource.ReportRemoteDatasource
import com.wheretogo.data.feature.dataErrorCatching
import com.wheretogo.data.model.report.RemoteReport
import javax.inject.Inject

class MockReportRemoteDatasourceImpl @Inject constructor() : ReportRemoteDatasource {
    private var newReportGroup = mutableListOf<RemoteReport>()
    override suspend fun addReport(report: RemoteReport): Result<Unit> {
        return dataErrorCatching {
            newReportGroup.add(report)
        }.mapCatching { Unit }
    }

    override suspend fun getReport(reportId: String): Result<RemoteReport> {
        return dataErrorCatching { newReportGroup.firstOrNull { it.reportId == reportId } }
    }

    override suspend fun removeReport(reportId: String): Result<Unit> {
        return dataErrorCatching {
            newReportGroup.removeIf { it.reportId == reportId }
        }.mapCatching { Unit }
    }

    override suspend fun getReportByType(reportType: String): Result<List<RemoteReport>> {
        return dataErrorCatching { newReportGroup.filter { it.type == reportType } }
    }

    override suspend fun getReportByStatus(reportStatus: String): Result<List<RemoteReport>> {
        return dataErrorCatching { newReportGroup.filter { it.status == reportStatus } }
    }

    override suspend fun getReportByUid(userId: String): Result<List<RemoteReport>> {
        return dataErrorCatching { newReportGroup.filter { it.userId == userId } }
    }
}