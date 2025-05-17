package com.wheretogo.domain.model.dummy

import com.wheretogo.domain.model.map.Comment

fun getCommentDummy(groupId: String = "cp1"): List<Comment> {
    return listOf(
        Comment(
            commentId = "cm1",
            userId = "uid1",
            userName = "하니팜하니",
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
            userName = "왜쳐다봐강해륀",
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
            userName = "다니엘리프",
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
            userName = "혜인붕어빵",
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
            userName = "고독한여행가",
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
            userName = "킴민지또디스해",
            groupId = groupId,
            emoji = "\uD83D\uDE10",
            detailedReview = "자세한 리뷰가 오는 공간입니다 자세한 리뷰가 오는 공간입니다 자세한 리뷰가 오는 공간입니다.",
            oneLineReview = "한줄평이 오는 공간입니다.",
            date = System.currentTimeMillis(),
            like = 22
        ),
    )
}