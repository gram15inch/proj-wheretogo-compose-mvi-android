package com.wheretogo.presentation.state

import com.wheretogo.domain.model.map.SlideItem

data class PopUpState(
    val initPage:Int? = null,
    val slideItems:List<SlideItem> = emptyList(),
    val commentState: CommentState = CommentState()
)