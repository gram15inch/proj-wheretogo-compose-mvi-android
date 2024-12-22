package com.wheretogo.data.model.history

import com.wheretogo.domain.HistoryType

data class RemoteHistoryGroupWrapper(
    val historyIdGroup: List<String> = emptyList(),
    val type: HistoryType = HistoryType.LIKE
)