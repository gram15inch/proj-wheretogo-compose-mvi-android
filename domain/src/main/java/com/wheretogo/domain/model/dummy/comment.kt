package com.wheretogo.domain.model.dummy

import com.wheretogo.domain.model.map.Comment

fun getCommentDummy(groupId: String = "cp1"): List<Comment> {
    return listOf(
        Comment(
            commentId = "cm1",
            userId = "uid1",
            groupId = groupId,
            emoji = "\uD83D\uDE19",
            detailedReview = "자세한 리뷰가 오는 공간입니다 자세한 리뷰가 오는 공간입니다 자세한 리뷰가 오는 공간입니다.",
            oneLineReview = "한줄평이 오는 공간입니다.",
            date = System.currentTimeMillis(),
            like = 3
        ),
        Comment(
            commentId = "cm2",
            userId = "uid2",
            groupId = groupId,
            emoji = "\uD83D\uDE10",
            detailedReview = "자세한 리뷰가 오는 공간입니다 자세한 리뷰가 오는 공간입니다 자세한 리뷰가 오는 공간입니다.",
            oneLineReview = "한줄평이 오는 공간입니다.",
            date = System.currentTimeMillis(),
            like = 5
        ),
        Comment(
            commentId = "cm3",
            userId = "uid3",
            groupId = groupId,
            emoji = "\uD83D\uDE07",
            detailedReview = "자세한 리뷰가 오는 공간입니다 자세한 리뷰가 오는 공간입니다 자세한 리뷰가 오는 공간입니다.",
            oneLineReview = "한줄평이 오는 공간입니다.",
            date = System.currentTimeMillis(),
            like = 0
        ),
        Comment(
            commentId = "cm4",
            userId = "uid4",
            groupId = groupId,
            emoji = "\uD83D\uDE07",
            detailedReview = "자세한 리뷰가 오는 공간입니다 자세한 리뷰가 오는 공간입니다 자세한 리뷰가 오는 공간입니다.",
            oneLineReview = "한줄평이 오는 공간입니다.",
            date = System.currentTimeMillis(),
            like = 0
        ),
        Comment(
            commentId = "cm5",
            userId = "uid5",
            groupId = groupId,
            emoji = "\uD83D\uDE10",
            detailedReview = "자세한 리뷰가 오는 공간입니다 자세한 리뷰가 오는 공간입니다 자세한 리뷰가 오는 공간입니다.",
            oneLineReview = "한줄평이 오는 공간입니다.",
            date = System.currentTimeMillis(),
            like = 40
        ),
        Comment(
            commentId = "cm6",
            userId = "uid6",
            groupId = groupId,
            emoji = "\uD83D\uDE10",
            detailedReview = "자세한 리뷰가 오는 공간입니다 자세한 리뷰가 오는 공간입니다 자세한 리뷰가 오는 공간입니다.",
            oneLineReview = "한줄평이 오는 공간입니다.",
            date = System.currentTimeMillis(),
            like = 22
        ),
    )
}