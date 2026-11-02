package com.wheretogo.data.model.history

import com.wheretogo.data.DataHistoryType
import kotlinx.serialization.Serializable

@Serializable
data class RemoteHistoryGroupWrapper(
    val type: DataHistoryType = DataHistoryType.LIKE,
    val historyIdGroup: Map<String, List<String>> = emptyMap(),
    val lastAddedAt: Long = 0L
)