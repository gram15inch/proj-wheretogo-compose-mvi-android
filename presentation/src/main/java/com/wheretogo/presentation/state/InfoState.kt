package com.wheretogo.presentation.state

import com.wheretogo.domain.model.map.CheckPoint
import com.wheretogo.domain.model.map.Course

data class InfoState(
    val isRemoveButton: Boolean = false,
    val isReportButton: Boolean = false,
    val isCourseInfo: Boolean = true,
    val reason: String = "",
    val course: Course = Course(),
    val checkPoint: CheckPoint = CheckPoint()
)