package com.wheretogo.domain.model.history

data class HistoryIdGroup(
    val groupById: Map<String, HashSet<String>> = emptyMap(),
)