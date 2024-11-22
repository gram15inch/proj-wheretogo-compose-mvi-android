package com.wheretogo.presentation.model


import com.wheretogo.domain.model.map.Comment
import com.wheretogo.domain.model.map.Course
import com.wheretogo.domain.model.map.Journey
import com.wheretogo.domain.model.map.LatLng
import com.kakao.vectormap.LatLng as KakaoLatLng
import com.naver.maps.geometry.LatLng as NaverLatLng

fun Course.toNaver(): CourseNaver {
    return CourseNaver(
        start = NaverLatLng(start.latitude, start.longitude),
        goal = NaverLatLng(goal.latitude, goal.longitude),
        waypoints = waypoints.toNaver()
    )
}

fun Course.toKakao(): CourseKakao {
    return CourseKakao(
        start = KakaoLatLng.from(start.latitude, start.longitude),
        goal = KakaoLatLng.from(goal.latitude, goal.longitude),
        waypoints = waypoints.toKakao()
    )
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


fun getJourneyDummy(): List<Journey> {
    return listOf(
        Journey(1001, "운전연수 코스 1001", "20", emptyList(), "",),
        Journey(1002, "운전연수 코스 1002", "15", emptyList(), "",),
    )
}