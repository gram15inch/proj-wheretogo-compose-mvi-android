package com.wheretogo.presentation


import com.wheretogo.domain.model.map.Comment
import com.wheretogo.domain.model.map.LatLng
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


fun getCommentDummy(): List<Comment> {
    return listOf(
        Comment(
            commentId = 1,
            userId = 2024,
            checkpointId = 101,
            imoge = "\uD83D\uDE19",
            detailedReview = "자세한 리뷰가 오는 공간입니다 자세한 리뷰가 오는 공간입니다 자세한 리뷰가 오는 공간입니다.",
            singleLineReview = "한줄평이 오는 공간입니다.",
            date = System.currentTimeMillis(),
            like = 3,
            isLike = true,
            isFold = true
        ),
        Comment(
            commentId = 2,
            userId = 2024,
            checkpointId = 101,
            imoge = "\uD83D\uDE10",
            detailedReview = "자세한 리뷰가 오는 공간입니다 자세한 리뷰가 오는 공간입니다 자세한 리뷰가 오는 공간입니다.",
            singleLineReview = "한줄평이 오는 공간입니다.",
            date = System.currentTimeMillis(),
            like = 5,
            isLike = false,
            isFold = true
        ),
        Comment(
            commentId = 3,
            userId = 2024,
            checkpointId = 102,
            imoge = "\uD83D\uDE07",
            detailedReview = "자세한 리뷰가 오는 공간입니다 자세한 리뷰가 오는 공간입니다 자세한 리뷰가 오는 공간입니다.",
            singleLineReview = "한줄평이 오는 공간입니다.",
            date = System.currentTimeMillis(),
            like = 0,
            isLike = true,
            isFold = false
        ),
        Comment(
            commentId = 4,
            userId = 2025,
            checkpointId = 103,
            imoge = "\uD83D\uDE07",
            detailedReview = "자세한 리뷰가 오는 공간입니다 자세한 리뷰가 오는 공간입니다 자세한 리뷰가 오는 공간입니다.",
            singleLineReview = "한줄평이 오는 공간입니다.",
            date = System.currentTimeMillis(),
            like = 0,
            isLike = false,
            isFold = true
        ),
        Comment(
            commentId = 5,
            userId = 2025,
            checkpointId = 104,
            imoge = "\uD83D\uDE10",
            detailedReview = "자세한 리뷰가 오는 공간입니다 자세한 리뷰가 오는 공간입니다 자세한 리뷰가 오는 공간입니다.",
            singleLineReview = "한줄평이 오는 공간입니다.",
            date = System.currentTimeMillis(),
            like = 40,
            isLike = false,
            isFold = true
        ),
        Comment(
            commentId = 6,
            userId = 2025,
            checkpointId = 104,
            imoge = "\uD83D\uDE10",
            detailedReview = "자세한 리뷰가 오는 공간입니다 자세한 리뷰가 오는 공간입니다 자세한 리뷰가 오는 공간입니다.",
            singleLineReview = "한줄평이 오는 공간입니다.",
            date = System.currentTimeMillis(),
            like = 22,
            isLike = false,
            isFold = true
        ),
    )
}