package com.wheretogo.domain.model.map

data class History(
    val commentGroup: HashSet<String> = hashSetOf(),
    val likeGroup: HashSet<String> = hashSetOf(),
    val bookmarkGroup: HashSet<String> = hashSetOf(),
    val reportCommentGroup: HashSet<String> = hashSetOf(),
)