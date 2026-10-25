package com.wheretogo.presentation.state

import com.wheretogo.presentation.DriveFloatHighlight
import com.wheretogo.presentation.DriveFloatingVisibleMode
import java.util.EnumSet

data class FloatingButtonState(
    val highlight: DriveFloatHighlight = DriveFloatHighlight.NONE,
    val stateMode: DriveFloatingVisibleMode = DriveFloatingVisibleMode.Default
) {
    companion object {
        val commentVisible: EnumSet<DriveFloatingVisibleMode> =
            EnumSet.of(DriveFloatingVisibleMode.Popup)
        val infoVisible: EnumSet<DriveFloatingVisibleMode> =
            EnumSet.of(DriveFloatingVisibleMode.Default, DriveFloatingVisibleMode.Popup)
        val exportVisible: EnumSet<DriveFloatingVisibleMode> = EnumSet.of(
            DriveFloatingVisibleMode.Default
        )
        val foldVisible: EnumSet<DriveFloatingVisibleMode> = EnumSet.of(
            DriveFloatingVisibleMode.Default,
            DriveFloatingVisibleMode.Popup
        )
    }
}