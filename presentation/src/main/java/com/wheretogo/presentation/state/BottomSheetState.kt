package com.wheretogo.presentation.state

import com.wheretogo.presentation.DriveBottomSheetContent
import com.wheretogo.presentation.state.CourseAddScreenState.CourseAddSheetState

data class BottomSheetState(
    val content: DriveBottomSheetContent = DriveBottomSheetContent.EMPTY,
    val checkPointAddState: CheckPointAddState = CheckPointAddState(),
    val courseAddSheetState: CourseAddSheetState = CourseAddSheetState(),
    val infoState: InfoState = InfoState(),
)
