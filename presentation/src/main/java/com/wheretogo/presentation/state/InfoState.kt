package com.wheretogo.presentation.state

import com.wheretogo.domain.model.report.ReportReason

data class InfoState(
    val isRemoveButton: Boolean = false,
    val isReportButton: Boolean = false,
    val isLoading : Boolean = false,
    val createdBy:String = "",
    val reason: ReportReason = ReportReason.SPAM,
)