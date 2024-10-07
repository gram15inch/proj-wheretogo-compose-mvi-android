package com.wheretogo.data.model.naver

data class NaverRouteWaypointResponse(
    val code: Int,
    val currentDateTime: String?,
    val message: String,
    val route: RouteX
)