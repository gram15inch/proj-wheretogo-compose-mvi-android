package com.wheretogo.presentation


import com.wheretogo.domain.model.map.Comment
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.presentation.state.DriveScreenState
import com.kakao.vectormap.LatLng as KakaoLatLng
import com.naver.maps.geometry.LatLng as NaverLatLng


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

fun DriveScreenState.PopUpState.CommentState.CommentAddState.toComment(): Comment {
    return Comment(
        emoji = this.largeEmoji.ifEmpty { emogiGroup.firstOrNull() ?: "" },
        oneLineReview = if (CommentType.ONE == commentType) editText.text else this.oneLineReview,
        detailedReview = if (CommentType.DETAIL == commentType) editText.text else this.detailReview,
        date = System.currentTimeMillis()
    )
}
