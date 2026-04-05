package com.dhkim139.admin.wheretogo.feature.report.data

import com.dhkim139.admin.wheretogo.core.AdminApi
import com.dhkim139.admin.wheretogo.feature.dashboard.DashboardStatsDto
import com.dhkim139.admin.wheretogo.feature.report.model.Report
import com.dhkim139.admin.wheretogo.feature.report.model.ReportAction
import com.dhkim139.admin.wheretogo.feature.report.model.ReportActionRequest
import com.dhkim139.admin.wheretogo.feature.report.model.ReportInput
import com.dhkim139.admin.wheretogo.feature.report.model.ReportListResponse
import javax.inject.Inject

class ReportRepositoryImpl @Inject constructor(
    private val adminApi: AdminApi,
) : ReportRepository {

    override suspend fun getDashboardStats(): Result<DashboardStatsDto> =
        runCatching { adminApi.getDashboardStats().data }

    override suspend fun getReports(
        status: String?,
        moderate: String?,
        page: Int,
        size: Int,
    ): Result<ReportListResponse> = runCatching {
        adminApi.getReports(status, moderate, page, size).data
    }

    override suspend fun getReport(reportId: String): Result<Report> = runCatching {
        adminApi.getReport(reportId).data.toDomain()
    }

    override suspend fun approveReport(
        reportId: String, withUser: Boolean, input: ReportInput
    ): Result<Unit> = runCatching {
        val actions = mutableListOf(ReportAction.APPROVE, ReportAction.HIDE_CONTENT)
        // 신고자 차단
        if (withUser) {
            actions.add(ReportAction.SUSPEND_REPORTER)
        }
        adminApi.actionReport(
            reportId = reportId, request = ReportActionRequest(
                reportId = reportId,
                actions = actions.map { it.name },
                duration = input.duration,
                reason = input.reason,
            )
        )
    }

    override suspend fun rejectReport(
        reportId: String, withUser: Boolean, input: ReportInput
    ): Result<Unit> = runCatching {
        val actions = mutableListOf(ReportAction.REJECT, ReportAction.SHOW_CONTENT)
        // 작성자 차단
        if (withUser) {
            actions.add(ReportAction.SUSPEND_WRITER)
        }
        adminApi.actionReport(
            reportId = reportId, request = ReportActionRequest(
                reportId = reportId,
                actions = actions.map { it.name },
                duration = input.duration,
                reason = input.reason,
            )
        )
    }
}
