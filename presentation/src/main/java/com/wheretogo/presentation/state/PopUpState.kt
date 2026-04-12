package com.wheretogo.presentation.state

import com.wheretogo.presentation.model.SlideItem

data class PopUpState(
    val initPage:Int? = null,
    val slideItems:List<SlideItem> = emptyList(),
    val commentState: CommentState = CommentState()
)