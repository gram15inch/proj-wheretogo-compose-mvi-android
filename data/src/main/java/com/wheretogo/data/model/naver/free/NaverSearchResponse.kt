package com.wheretogo.data.model.naver.free

data class NaverSearchResponse(
    val display: Int,
    val items: List<Item>,
    val lastBuildDate: String,
    val start: Int,
    val total: Int
)