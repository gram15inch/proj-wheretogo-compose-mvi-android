package com.wheretogo.data.model.journey


data class LocalCourse(val code: Int, val start: LocalLatLng, val goal: LocalLatLng, val waypoints: List<LocalLatLng>)
