package com.wheretogo.presentation.state

import com.wheretogo.presentation.DriveBottomSheetContent
import com.wheretogo.presentation.state.CourseAddScreenState.CourseAddSheetState

data class BottomSheetState(
    val isVisible: Boolean = false,
    val isSpaceVisibleWhenClose: Boolean = false,
    val initHeight: Int = 0,
    val content: DriveBottomSheetContent = DriveBottomSheetContent.EMPTY,
    val checkPointAddState: CheckPointAddState = CheckPointAddState(),
    val courseAddSheetState: CourseAddSheetState = CourseAddSheetState(),
    val infoState: InfoState = InfoState()
)
