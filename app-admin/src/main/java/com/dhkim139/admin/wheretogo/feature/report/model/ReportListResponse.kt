package com.dhkim139.admin.wheretogo.feature.report.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReportListResponse(
    val reports: List<ReportDto>,
    val total: Int,
    val page: Int,
    val hasNext: Boolean,
)