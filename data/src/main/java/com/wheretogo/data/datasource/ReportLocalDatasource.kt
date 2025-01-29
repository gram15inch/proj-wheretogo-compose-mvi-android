package com.wheretogo.data.datasource

import com.wheretogo.data.model.report.LocalReport

interface ReportLocalDatasource {
    suspend fun addReport(report: LocalReport)
    suspend fun getReport(reportId: String): LocalReport?
    suspend fun removeReport(reportId: String)
}