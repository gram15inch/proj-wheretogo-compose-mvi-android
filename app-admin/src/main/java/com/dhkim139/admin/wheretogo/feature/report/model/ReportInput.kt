package com.dhkim139.admin.wheretogo.feature.report.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReportInput(
    val duration: Int? = null,
    val reason: String? = null,
)