package com.wheretogo.domain.model.history

import com.wheretogo.domain.HistoryType

data class HistoryGroupWrapper(
    val type: HistoryType = HistoryType.LIKE,
    val historyIdGroup: HistoryIdGroup = HistoryIdGroup(),
    val lastAddedAt: Long = 0L
)