package com.wheretogo.data.model.history

data class LocalHistory(
    val course: LocalHistoryGroupWrapper = LocalHistoryGroupWrapper(),
    val checkpoint: LocalHistoryGroupWrapper = LocalHistoryGroupWrapper(),
    val comment: LocalHistoryGroupWrapper = LocalHistoryGroupWrapper(),
    val like: LocalHistoryGroupWrapper = LocalHistoryGroupWrapper(),
    val bookmark: LocalHistoryGroupWrapper = LocalHistoryGroupWrapper(),
    val report: LocalHistoryGroupWrapper = LocalHistoryGroupWrapper(),
)