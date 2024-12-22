package com.wheretogo.domain.model.map

data class History(
    val commentGroup: List<String> = emptyList(),
    val likeGroup: List<String> = emptyList(),
    val bookmarkGroup: List<String> = emptyList()
)