package com.wheretogo.presentation


import com.naver.maps.map.overlay.Marker
import com.wheretogo.domain.AuthCompany
import com.wheretogo.domain.CourseDetail
import com.wheretogo.domain.OverlayType

import com.wheretogo.domain.RouteDetailType
import com.wheretogo.domain.model.map.Comment
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.model.map.RouteWaypointItem
import com.wheretogo.presentation.model.OverlayTag
import com.wheretogo.presentation.state.CommentState.CommentAddState
import com.wheretogo.presentation.state.CourseAddScreenState.RouteWaypointItemState
import com.kakao.vectormap.LatLng as KakaoLatLng
import com.naver.maps.geometry.LatLng as NaverLatLng

fun LatLng.toMarker():Marker{
    return Marker().apply {
        tag = "${latitude}${longitude}"
        position = toNaver()
    }
}

fun List<LatLng>.toNaver(): List<NaverLatLng> {
    return this.map { NaverLatLng(it.latitude, it.longitude) }
}

fun List<LatLng>.toKakao(): List<KakaoLatLng> {
    return this.map { KakaoLatLng.from(it.latitude, it.longitude) }
}

fun NaverLatLng.toDomainLatLng(): LatLng {
    return LatLng(latitude, longitude)
}

fun LatLng.toNaver(): NaverLatLng {
    return NaverLatLng(latitude, longitude)
}

fun CommentAddState.toComment(): Comment {
    return Comment(
        groupId = this.groupId,
        emoji = this.largeEmoji.ifEmpty { emogiGroup.firstOrNull() ?: "" },
        oneLineReview = if (CommentType.ONE == commentType) editText.text else this.oneLineReview,
        detailedReview = if (CommentType.DETAIL == commentType) editText.text else this.detailReview,
        date = System.currentTimeMillis()
    )
}

fun RouteWaypointItem.toRouteWaypointItemState(): RouteWaypointItemState {
    return RouteWaypointItemState(
        data = this
    )
}

fun RouteWaypointItemState.toRouteWaypointItem(): RouteWaypointItem {
    return this.data
}

fun RouteDetailType.toStrRes(): Int {
    return when (this) {
        RouteDetailType.TYPE -> R.string.category
        RouteDetailType.LEVEL -> R.string.level
        RouteDetailType.RECOMMEND -> R.string.recommend
        else -> R.string.unknown
    }
}

fun CourseDetail.toStrRes(): Int {
    return when (this.type) {
        RouteDetailType.TYPE -> {
            when (this) {
                CourseDetail.DRIVE -> R.string.drive
                CourseDetail.SPORT -> R.string.sports
                CourseDetail.TRAINING -> R.string.training
                else -> R.string.drive
            }
        }

        RouteDetailType.LEVEL -> {
            when (this) {
                CourseDetail.BEGINNER -> R.string.beginner
                CourseDetail.LOVER -> R.string.lover
                CourseDetail.EXPERT -> R.string.expert
                CourseDetail.PRO -> R.string.pro
                else -> R.string.beginner
            }
        }

        RouteDetailType.RECOMMEND -> {
            when (this) {
                CourseDetail.SOLO -> R.string.solo
                CourseDetail.FRIEND -> R.string.friend
                CourseDetail.FAMILY -> R.string.family
                CourseDetail.COUPLE -> R.string.couple
                else -> R.string.solo
            }
        }

        else -> {
            R.string.unknown
        }
    }
}

fun parseLogoImgRes(company: String): Int {
    val auth = try {
        AuthCompany.valueOf(company)
    } catch (e: Exception) {
        AuthCompany.GOOGLE
    }

    return when (auth) {
        AuthCompany.GOOGLE -> {
            R.drawable.ic_heart_line
        }
        else->{
            R.drawable.lg_app
        }
    }
}

fun OverlayTag.toStringTag() = "${this.overlayId}/${this.parentId}/${this.overlayType}/${this.iconType}/${this.latlng.latitude}:${this.latlng.longitude}"

fun OverlayTag.Companion.parse(stringTag: String): OverlayTag? {
    return try {
        val list = stringTag.split("/")
        val latLng= list[4].split(":")
        OverlayTag(
            list[0],
            list[1],
            OverlayType.valueOf(list[2]),
            MarkerIconType.valueOf(list[3]),
            LatLng(latLng[0].toDouble(),latLng[1].toDouble())
        )
    } catch (e: Exception) {
        null
    }
}
