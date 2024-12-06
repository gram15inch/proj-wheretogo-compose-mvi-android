package com.wheretogo.domain.model.map

data class MetaCheckPoint(
    val metaCheckPointGroup: List<String> = emptyList(),
    val timeStamp: Long = 0L
)