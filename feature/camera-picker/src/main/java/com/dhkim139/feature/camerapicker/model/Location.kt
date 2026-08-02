package com.dhkim139.feature.camerapicker.model

data class Location(
    val lat: Double,
    val lng: Double,
    val accuracy: Float, // Meter
    val createAt: Long
)
