package com.wheretogo.domain.model.map

data class History(
    val courseGroup: HashSet<String> = hashSetOf(),
    val checkpointGroup: HashSet<String> = hashSetOf(),
    val commentGroup: HashSet<String> = hashSetOf(),
    val likeGroup: HashSet<String> = hashSetOf(),
    val bookmarkGroup: HashSet<String> = hashSetOf(),
    val reportGroup: HashSet<String> = hashSetOf(),
)