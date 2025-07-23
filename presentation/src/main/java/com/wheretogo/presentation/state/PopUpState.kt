package com.wheretogo.presentation.state

import android.net.Uri

data class PopUpState(
    val isVisible: Boolean = false,
    val checkPointId: String = "",
    val imageUri: Uri? = null,
    val commentState: CommentState = CommentState()
)