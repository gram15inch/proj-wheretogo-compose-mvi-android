package com.dhkim139.wheretogo.data.model.naver

data class NaverRouteResponse(
    val code: Int,
    val currentDateTime: String,
    val message: String,
    val route: Route
)