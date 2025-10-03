package com.wheretogo.data.model.history

import com.wheretogo.domain.HistoryType

data class RemoteHistoryGroupWrapper(
    val type: HistoryType = HistoryType.LIKE,
    val historyIdGroup: Map<String, List<String>> = emptyMap(),
    val lastAddedAt: Long = 0L
)