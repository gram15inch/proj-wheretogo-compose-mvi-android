package com.wheretogo.domain.model.util

data class Snapshot(
    val indexIdGroup: List<String> = emptyList(),
    val refId: String = "",
    val updateAt: Long = 0L
)