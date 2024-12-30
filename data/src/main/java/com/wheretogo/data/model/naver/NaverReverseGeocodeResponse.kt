package com.wheretogo.data.model.naver

data class NaverReverseGeocodeResponse(
    val results: List<Result>,
    val status: Status
)