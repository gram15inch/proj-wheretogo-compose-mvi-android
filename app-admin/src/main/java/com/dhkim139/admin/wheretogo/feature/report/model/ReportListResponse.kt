package com.dhkim139.admin.wheretogo.feature.report.model

import kotlinx.serialization.Serializable

@Serializable
data class ReportListResponse(
    val reports: List<ReportDto>,
    val total: Int,
    val page: Int,
    val hasNext: Boolean,
)