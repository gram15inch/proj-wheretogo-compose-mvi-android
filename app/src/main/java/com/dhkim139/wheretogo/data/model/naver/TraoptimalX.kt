package com.dhkim139.wheretogo.data.model.naver

data class TraoptimalX(
    val guide: List<GuideX>,
    val path: List<List<Double>>,
    val section: List<SectionX>?,
    val summary: SummaryX
)