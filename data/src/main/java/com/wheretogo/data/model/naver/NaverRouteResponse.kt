package com.wheretogo.data.model.naver

data class NaverRouteResponse(
    val code: Int,
    val currentDateTime: String,
    val message: String,
    val route: Route
)