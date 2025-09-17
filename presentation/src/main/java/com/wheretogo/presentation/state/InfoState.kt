package com.wheretogo.presentation.state

data class InfoState(
    val isRemoveButton: Boolean = false,
    val isReportButton: Boolean = false,
    val isLoading : Boolean = false,
    val createdBy:String = "",
    val reason: String = "",
)