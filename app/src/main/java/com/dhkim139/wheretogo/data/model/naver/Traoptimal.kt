package com.dhkim139.wheretogo.data.model.naver

data class Traoptimal(
    val guide: List<Guide>,
    val path: List<List<Double>>,
    val section: List<Section>,
    val summary: Summary
)