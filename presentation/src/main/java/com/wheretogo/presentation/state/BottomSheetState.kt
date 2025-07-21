package com.wheretogo.presentation.state

import com.wheretogo.presentation.BottomSheetContent
import com.wheretogo.presentation.state.CourseAddScreenState.CourseAddState

data class BottomSheetState(
    val isVisible: Boolean = false,
    val isSpaceVisibleWhenClose: Boolean = false,
    val initHeight: Int = 0,
    val content: BottomSheetContent = BottomSheetContent.EMPTY,
    val checkPointAddState: CheckPointAddState = CheckPointAddState(),
    val courseAddState: CourseAddState = CourseAddState(),
    val infoState: InfoState = InfoState()
)
