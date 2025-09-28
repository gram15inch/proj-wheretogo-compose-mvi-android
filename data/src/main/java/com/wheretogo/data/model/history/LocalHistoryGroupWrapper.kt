package com.wheretogo.data.model.history

import com.wheretogo.domain.HistoryType

data class LocalHistoryGroupWrapper(
    val type: HistoryType = HistoryType.LIKE,
    val historyIdGroup: HashSet<String> = hashSetOf(),
    val lastAddedAt: Long = 0L
)