package com.wheretogo.domain.model.map

data class Snapshot(
    val indexIdGroup: List<String> = emptyList(),
    val refId: String = "",
    val timeStamp: Long = 0L
)