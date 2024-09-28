package com.dhkim139.wheretogo.data.model.naver

data class SummaryX(
    val bbox: List<List<Double>>,
    val departureTime: String,
    val distance: Int,
    val duration: Int,
    val fuelPrice: Int,
    val goal: GoalX,
    val start: StartX,
    val taxiFare: Int,
    val tollFare: Int
)