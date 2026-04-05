package com.dhkim139.admin.wheretogo.feature.dashboard

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DashboardStatsDto(
    val pendingCount: Int,
    val approvedToday: Int,
    val highCount: Int,
    val mediumCount: Int,
)