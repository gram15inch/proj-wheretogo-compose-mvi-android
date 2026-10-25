package com.wheretogo.presentation.state

import com.wheretogo.presentation.DriveBottomSheetContent

data class BottomSheetState(
    val content: DriveBottomSheetContent = DriveBottomSheetContent.EMPTY,
    val infoState: InfoState = InfoState(),
)
