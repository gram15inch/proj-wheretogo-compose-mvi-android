package com.wheretogo.domain.model.map

import androidx.annotation.StringRes
import com.wheretogo.domain.CourseDetail
import com.wheretogo.domain.RouteDetailType

data class RouteDetailItem(
    val code: String = CourseDetail.DRIVE.code,
    val type: RouteDetailType = RouteDetailType.TAG,
    val emogi: String = "",
    @StringRes
    val strRes: Int = 0,
)