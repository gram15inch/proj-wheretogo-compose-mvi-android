package com.wheretogo.data.model.comment

import com.wheretogo.data.DATA_NULL

data class RemoteCommentGroupWrapper(
    val groupId: String = DATA_NULL,
    val remoteCommentGroup: List<RemoteComment> = emptyList()
)