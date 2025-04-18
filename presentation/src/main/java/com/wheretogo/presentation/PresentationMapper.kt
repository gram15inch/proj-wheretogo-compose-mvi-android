package com.wheretogo.presentation


import com.wheretogo.domain.AuthCompany
import com.wheretogo.domain.CourseDetail
import com.wheretogo.domain.RouteDetailType
import com.wheretogo.domain.model.map.Comment
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.presentation.state.CommentState.CommentAddState
import com.kakao.vectormap.LatLng as KakaoLatLng
import com.naver.maps.geometry.LatLng as NaverLatLng


fun List<LatLng>.toNaver(): List<NaverLatLng> {
    return this.map { NaverLatLng(it.latitude, it.longitude) }
}

@Suppress("unused")
fun List<LatLng>.toKakao(): List<KakaoLatLng> {
    return this.map { KakaoLatLng.from(it.latitude, it.longitude) }
}

fun List<NaverLatLng>.toDomain():List<LatLng>{
    return map{it.toDomainLatLng()}
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
