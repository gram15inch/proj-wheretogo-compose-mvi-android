package com.wheretogo.data.model.history

import com.wheretogo.data.DataHistoryType

data class LocalHistoryGroupWrapper(
    val type: DataHistoryType = DataHistoryType.LIKE,
    val historyIdGroup: LocalHistoryIdGroup = LocalHistoryIdGroup(),
    val lastAddedAt: Long = 0L
)