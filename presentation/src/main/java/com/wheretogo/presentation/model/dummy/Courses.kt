package com.wheretogo.presentation.model.dummy

import com.wheretogo.domain.CourseDetail
import com.wheretogo.domain.RouteDetailType
import com.wheretogo.domain.model.map.RouteDetailItem
import com.wheretogo.presentation.R

private val routeTAGGroup = listOf(
    RouteDetailItem(
        code = CourseDetail.DRIVE.code,
        type = RouteDetailType.TAG,
        emogi = "\uD83D\uDCCD",
        strRes = R.string.drive
    ),
    RouteDetailItem(
        code = CourseDetail.SPORT.code,
        type = RouteDetailType.TAG,
        emogi = "\uD83C\uDFCE\uFE0F",
        strRes = R.string.sports
    ),
    RouteDetailItem(
        code = CourseDetail.TRAINING.code,
        type = RouteDetailType.TAG,
        emogi = "\uD83D\uDD30",
        strRes = R.string.training
    )
)
private val routeLevelGroup = listOf(
    RouteDetailItem(
        code = CourseDetail.BEGINNER.code,
        type = RouteDetailType.LEVEL,
        emogi = "\uD83C\uDF31",
        strRes = R.string.beginner
    ),
    RouteDetailItem(
        code = CourseDetail.LOVER.code,
        type = RouteDetailType.LEVEL,
        emogi = "\uD83C\uDFC3",
        strRes = R.string.lover
    ),
    RouteDetailItem(
        code = CourseDetail.EXPERT.code,
        type = RouteDetailType.LEVEL,
        emogi = "\uD83C\uDFC7",
        strRes = R.string.expert
    ),
    RouteDetailItem(
        code = CourseDetail.PRO.code,
        type = RouteDetailType.LEVEL,
        emogi = "\uD83D\uDCCD",
        strRes = R.string.pro
    ),
)
private val routeRecommendGroup = listOf(
    RouteDetailItem(
        code = CourseDetail.SOLO.code,
        type = RouteDetailType.RECOMMEND,
        emogi = "\uD83C\uDFCE\uFE0F",
        strRes = R.string.solo
    ),
    RouteDetailItem(
        code = CourseDetail.FRIEND.code,
        type = RouteDetailType.RECOMMEND,
        emogi = "\uD83E\uDD3C",
        strRes = R.string.friend
    ),
    RouteDetailItem(
        code = CourseDetail.FAMILY.code,
        type = RouteDetailType.RECOMMEND,
        emogi = "\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC66",
        strRes = R.string.family
    ),
    RouteDetailItem(
        code = CourseDetail.COUPLE.code,
        type = RouteDetailType.RECOMMEND,
        emogi = "\uD83D\uDC91",
        strRes = R.string.couple
    )
)

fun getRouteDetailItemGroup() =
    routeTAGGroup +
    routeLevelGroup +
    routeRecommendGroup
