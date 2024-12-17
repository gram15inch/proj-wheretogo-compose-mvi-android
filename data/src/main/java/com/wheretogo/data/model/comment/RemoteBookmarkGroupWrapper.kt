package com.wheretogo.data.model.comment

data class RemoteBookmarkGroupWrapper(
    val uid: String = "",
    val bookmarkGroup: List<String> = emptyList()
)