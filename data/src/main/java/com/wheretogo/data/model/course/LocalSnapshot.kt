package com.wheretogo.data.model.course

data class LocalSnapshot(
    val indexIdGroup: List<String> = emptyList(),
    val refId: String = "",
    val timeStamp: Long = 0L
)