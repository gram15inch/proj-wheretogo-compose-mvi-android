package com.dhkim139.admin.wheretogo.feature.dashboard

import kotlinx.serialization.Serializable

@Serializable
data class DashboardStatsDto(
    val pendingCount: Int,
    val approvedToday: Int,
    val highCount: Int,
    val mediumCount: Int,
)