package com.wheretogo.presentation.state

data class PopUpState(
    val imagePath: String = "",
    val commentState: CommentState = CommentState()
)