package com.wheretogo.data.model.course

data class LocalSnapshot(
    val indexIdGroup: List<String> = emptyList(),
    val refId: String = "",
    val updateAt: Long = 0L
)