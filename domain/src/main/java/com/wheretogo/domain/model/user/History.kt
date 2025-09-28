package com.wheretogo.domain.model.user

import com.wheretogo.domain.model.history.HistoryGroupWrapper

data class History(
    val course: HistoryGroupWrapper = HistoryGroupWrapper(),
    val checkpoint: HistoryGroupWrapper = HistoryGroupWrapper(),
    val comment: HistoryGroupWrapper = HistoryGroupWrapper(),
    val like: HistoryGroupWrapper = HistoryGroupWrapper(),
    val bookmark: HistoryGroupWrapper = HistoryGroupWrapper(),
    val report: HistoryGroupWrapper = HistoryGroupWrapper(),
)