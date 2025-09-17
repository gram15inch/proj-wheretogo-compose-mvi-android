package com.wheretogo.data.datasource

import com.wheretogo.data.model.report.LocalReport

interface ReportLocalDatasource {
    suspend fun addReport(report: LocalReport): Result<Unit>
    suspend fun getReport(reportId: String): Result<LocalReport>
    suspend fun removeReport(reportId: String): Result<Unit>
}